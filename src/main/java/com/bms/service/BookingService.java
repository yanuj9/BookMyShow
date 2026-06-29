package com.bms.service;

import com.bms.concurrency.DistributedLockManager;
import com.bms.domain.Seat;
import com.bms.domain.SeatStatus;
import com.bms.domain.Show;
import com.bms.exception.*;
import com.bms.repository.ShowRepository;
import java.util.List;

public class BookingService {
    private final DistributedLockManager lockManager;
    private final ShowRepository showRepository;
    private final PaymentProcessor paymentProcessor;

    public BookingService(DistributedLockManager lockManager, ShowRepository showRepository, PaymentProcessor paymentProcessor) {
        this.lockManager = lockManager;
        this.showRepository = showRepository;
        this.paymentProcessor = paymentProcessor;
    }

    public String bookTickets(String showId, List<String> seatIds, String userId) {
        // 1. Sort IDs to prevent distributed deadlocks
        List<String> sortedSeatIds = seatIds.stream().sorted().toList();
        String lockKey = "LOCK:SHOW:" + showId + ":SEATS:" + String.join(",", sortedSeatIds);

        // 2. Acquire Redis Lock (Wait 2s, hold for 10s)
        if (!lockManager.acquireLock(lockKey, 2, 10)) {
            throw new LockAcquisitionException("Seats are currently heavily contested. Please try again.");
        }

        try {
            // 3. Enter "DB Transaction" - Check actual state
            Show show = showRepository.findById(showId);
            double totalAmount = 0;

            for (String seatId : sortedSeatIds) {
                Seat seat = show.getSeats().get(seatId);
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatUnavailableException("Seat " + seatId + " is already booked or locked.");
                }
                totalAmount += seat.getPrice();
                seat.setStatus(SeatStatus.LOCKED); // Optimistic temporary lock in DB
            }
            showRepository.save(show);

            // 4. Process Payment
            boolean paymentSuccess = paymentProcessor.processPayment(userId, totalAmount);

            if (!paymentSuccess) {
                // Rollback
                for (String seatId : sortedSeatIds) {
                    show.getSeats().get(seatId).setStatus(SeatStatus.AVAILABLE);
                }
                showRepository.save(show);
                throw new PaymentFailedException("Payment failed for user: " + userId);
            }

            // 5. Commit Booking
            for (String seatId : sortedSeatIds) {
                show.getSeats().get(seatId).setStatus(SeatStatus.BOOKED);
                show.getSeats().get(seatId).setBookedBy(userId);
            }
            showRepository.save(show);

            return "SUCCESS: Seats " + sortedSeatIds + " booked for " + userId;

        } finally {
            // 6. ALWAYS release lock
            lockManager.releaseLock(lockKey);
        }
    }
}
