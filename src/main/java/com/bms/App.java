package com.bms;

import com.bms.concurrency.RedisLockManager;
import com.bms.domain.*;
import com.bms.repository.InMemoryShowRepositoryImpl;
import com.bms.repository.InMemoryTheatreRepositoryImpl;
import com.bms.repository.ShowRepository;
import com.bms.service.BookingService;
import com.bms.service.PaymentProcessor;
import com.bms.service.TheatreService;
import com.esotericsoftware.minlog.Log;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    public static void main(String[] args) throws InterruptedException {
        // ==========================================
        // PHASE 1: INFRASTRUCTURE & DEPENDENCY SETUP
        // ==========================================
        logger.info("Initializing System & Redis Connection...");
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);

        // Repositories
        InMemoryTheatreRepositoryImpl theatreRepository = new InMemoryTheatreRepositoryImpl();
        ShowRepository showRepository = new InMemoryShowRepositoryImpl();

        // For DBs, etc
        // Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bookmyshow", "bms_user", "bms_password");
        // ShowRepository showRepository = new PostgresShowRepository(conn);

        // Services
        TheatreService theatreService = new TheatreService(theatreRepository);
        RedisLockManager lockManager = new RedisLockManager(redissonClient);
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        BookingService bookingService = new BookingService(lockManager, showRepository, paymentProcessor);

        // ==========================================
        // PHASE 2: MASTER DATA SEEDING
        // ==========================================
        logger.info("Seeding Master Data (Cities, Theatres, Shows)...");
        
        // 1. Create Location
        City bengaluru = new City("CITY_BLR", "Bengaluru", "Karnataka");
        theatreRepository.saveCity(bengaluru);
        City delhi = new City("CITY_DEL", "Delhi", "New Delhi");
        theatreRepository.saveCity(delhi);

        // 2. Create Seats & Screens
        Seat seatA1 = new Seat("SEAT_A1", "A1", 500.0);
        Seat seatA2 = new Seat("SEAT_A2", "A2", 500.0);
        Screen imaxScreen = new Screen("SCR_IMAX", "IMAX Audi 1", List.of(seatA1, seatA2));

        // 3. Create Theatre in City
        Theatre nexusMall = new Theatre("TH_NEXUS", "Nexus Mall Koramangala", "Koramangala, BLR", bengaluru.getId(), List.of(imaxScreen));
        theatreRepository.saveTheatre(nexusMall);

        // 4. Create a Show mapping to the Screen
        Map<String, Seat> showInventory = new ConcurrentHashMap<>();
        showInventory.put(seatA1.getSeatId(), seatA1);
        showInventory.put(seatA2.getSeatId(), seatA2);
        Show oppenheimerShow = new Show("SHOW_OPP_01", "MOV_OPPENHEIMER", imaxScreen.getId(), LocalDateTime.now().plusDays(1), showInventory);
        showRepository.save(oppenheimerShow);


        // ==============================================
        // PHASE 3: USER JOURNEY - DISCOVERY & BOOKING
        // ==============================================
        logger.info("\n=== USER JOURNEY: DISCOVERY ===");
        String targetCity = "Delhi";
        logger.info("User selects city: " + targetCity);
        
        try {
            List<Theatre> availableTheatres = theatreService.getTheatresInCity(targetCity);
    
            if (availableTheatres == null || availableTheatres.isEmpty()) {
                logger.info("No theatres or shows currently available in " + targetCity + ".");
                redissonClient.shutdown(); // Gracefully shut down Redis threads
                return; // Exit the application
            }

            for (Theatre t : availableTheatres) {
                logger.info("Found Theatre: " + t.getName() + " | Address: " + t.getAddress());
                for (Screen s : t.getScreens()) {
                    logger.info(" -> Checking Screen: " + s.getName());
                    for (Seat seat : s.getSeats()) {
                        logger.info("    - Seat: " + seat.getSeatNumber() + " | Price: Rs." + seat.getPrice());
                    }
                }
            }
        }
        catch (Exception e) {
            logger.info("Error in finding theatre in the city." + e.getMessage());
            redissonClient.shutdown();
            return ;
        }

        // ==========================================
        // PHASE 4: USER JOURNEY - CONCURRENT BOOKING
        // ==========================================
        logger.info("\n=== USER JOURNEY: CONCURRENT TRANSACTION ===");
        logger.info("Simulating a flash sale! 10 users attempting to book SEAT_A1 simultaneously...");

        int concurrentUsers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(concurrentUsers);
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 1; i <= concurrentUsers; i++) {
            final String userId = "User_00" + i;
            executor.submit(() -> {
                try {
                    // Makes sure the thread is created
                    readyLatch.countDown();
                    // Now the thread waits here
                    startLatch.await(); // Synchronize all threads to start at the exact same millisecond
                    
                    String result = bookingService.bookTickets(oppenheimerShow.getShowId(), List.of("SEAT_A1"), userId);
                    logger.info("[SUCCESS] " + result);
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    logger.info("[FAILED] " + userId + " could not book: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Make sure all threads are created
        // Main thread waits here indefinitely until ALL 10 threads have called readyLatch.countDown()
        readyLatch.await();
        Log.info("All threads are locked and loaded. Firing the starting gun!");
        startLatch.countDown(); 
        
        // Wait for all requests to finish
        endLatch.await();
        executor.shutdown();
        redissonClient.shutdown();

        // ==========================================
        // PHASE 5: VERIFICATION
        // ==========================================
        logger.info("\n=== TRANSACTION RESULTS ===");
        logger.info("Successful Bookings (Expected 1): " + successCount.get());
        logger.info("Failed Attempts (Expected 9): " + failCount.get());
        
        Seat finalSeatState = showRepository.findById(oppenheimerShow.getShowId()).getSeats().get("SEAT_A1");
        logger.info("Final state of SEAT_A1 in DB: " + finalSeatState.getStatus());
        
        if (successCount.get() == 1 && finalSeatState.getStatus() == SeatStatus.BOOKED) {
            logger.info("\n✅ CONCURRENCY TEST PASSED: No Lost Updates! Distributed Lock effectively defended the DB. Winner: " + finalSeatState.getBookedBy());
        } else {
            logger.info("\n❌ CONCURRENCY TEST FAILED: Data anomaly detected.");
        }
    }
}
