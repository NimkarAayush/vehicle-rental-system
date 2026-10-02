package com.vehiclerental.repository;
import com.vehiclerental.entity.Booking;
import com.vehiclerental.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    boolean existsByVehicleId(Long vehicleId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN :statuses " +
           "AND b.startDate < :endDate AND b.endDate > :startDate " +
           "AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId)")
    boolean existsOverlappingBooking(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("excludeBookingId") Long excludeBookingId);

    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN :statuses AND b.startDate >= :today ORDER BY b.startDate ASC")
    List<Booking> findUpcomingBookings(
            @Param("vehicleId") Long vehicleId, 
            @Param("statuses") List<BookingStatus> statuses, 
            @Param("today") LocalDate today);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findTop5ByOrderByCreatedAtDesc();
    long countByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicle.id = :vehicleId AND b.status = 'ACTIVE' AND b.id != :excludeBookingId")
    boolean existsOtherActiveBooking(@Param("vehicleId") Long vehicleId, @Param("excludeBookingId") Long excludeBookingId);
}
