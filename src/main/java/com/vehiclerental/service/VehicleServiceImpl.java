package com.vehiclerental.service;

import com.vehiclerental.dto.VehicleFormDto;
import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.entity.VehicleType;
import com.vehiclerental.exception.VehicleInUseException;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.repository.VehicleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Page<Vehicle> findAllFiltered(String search, VehicleStatus status, Pageable pageable) {
        Specification<Vehicle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate brandMatch = cb.like(cb.lower(root.get("brand")), searchPattern);
                Predicate modelMatch = cb.like(cb.lower(root.get("model")), searchPattern);
                Predicate regMatch = cb.like(cb.lower(root.get("registrationNumber")), searchPattern);
                predicates.add(cb.or(brandMatch, modelMatch, regMatch));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return vehicleRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Vehicle> findAvailableFiltered(VehicleType type, String brand, BigDecimal minPrice, BigDecimal maxPrice, java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable) {
        Specification<Vehicle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), VehicleStatus.AVAILABLE));
            
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (brand != null && !brand.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerDay"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerDay"), maxPrice));
            }

            if (startDate != null && endDate != null) {
                // Subquery: SELECT b.vehicle.id FROM Booking b WHERE b.status IN (CONFIRMED, ACTIVE) AND b.startDate <= endDate AND b.endDate >= startDate
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<com.vehiclerental.entity.Booking> bookingRoot = subquery.from(com.vehiclerental.entity.Booking.class);
                subquery.select(bookingRoot.get("vehicle").get("id"));

                List<Predicate> subPredicates = new ArrayList<>();
                subPredicates.add(bookingRoot.get("status").in(com.vehiclerental.entity.BookingStatus.CONFIRMED, com.vehiclerental.entity.BookingStatus.ACTIVE));
                subPredicates.add(cb.lessThanOrEqualTo(bookingRoot.get("startDate"), endDate));
                subPredicates.add(cb.greaterThanOrEqualTo(bookingRoot.get("endDate"), startDate));

                subquery.where(cb.and(subPredicates.toArray(new Predicate[0])));
                
                predicates.add(cb.not(root.get("id").in(subquery)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return vehicleRepository.findAll(spec, pageable);
    }

    @Override
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle Id:" + id));
    }

    @Override
    public Optional<Vehicle> getOptionalById(Long id) {
        return vehicleRepository.findById(id);
    }

    @Override
    @Transactional
    public Vehicle save(VehicleFormDto dto) {
        String imageUrl = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            imageUrl = fileStorageService.storeFile(dto.getImageFile());
        }

        Vehicle vehicle = Vehicle.builder()
                .brand(dto.getBrand())
                .model(dto.getModel())
                .type(dto.getType())
                .registrationNumber(dto.getRegistrationNumber())
                .pricePerDay(dto.getPricePerDay())
                .status(dto.getStatus())
                .imageUrl(imageUrl)
                .build();
                
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle update(Long id, VehicleFormDto dto) {
        Vehicle vehicle = findById(id);
        
        String oldImageUrl = vehicle.getImageUrl();
        String newImageUrl = oldImageUrl; // default to old image

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            newImageUrl = fileStorageService.storeFile(dto.getImageFile());
            if (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
                fileStorageService.deleteFile(oldImageUrl);
            }
        }

        vehicle.setBrand(dto.getBrand());
        vehicle.setModel(dto.getModel());
        vehicle.setType(dto.getType());
        vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        vehicle.setPricePerDay(dto.getPricePerDay());
        vehicle.setStatus(dto.getStatus());
        vehicle.setImageUrl(newImageUrl);
        
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (bookingRepository.existsByVehicleId(id)) {
            throw new VehicleInUseException("Cannot delete vehicle because it has booking history. Please set its status to MAINTENANCE instead.");
        }
        
        Vehicle vehicle = findById(id);
        String imageUrl = vehicle.getImageUrl();
        
        vehicleRepository.delete(vehicle);
        
        if (imageUrl != null) {
            fileStorageService.deleteFile(imageUrl);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = findById(id);
        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);
    }

    @Override
    public boolean isRegistrationNumberUnique(String registrationNumber, Long currentId) {
        if (currentId == null) {
            return !vehicleRepository.existsByRegistrationNumber(registrationNumber);
        }
        return !vehicleRepository.existsByRegistrationNumberAndIdNot(registrationNumber, currentId);
    }
}
