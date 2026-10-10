package com.chococraft.repository;

import com.chococraft.entity.CustomerOrder;
import com.chococraft.entity.User;
import com.chococraft.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserOrderByOrderDateDesc(User user);

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    Optional<CustomerOrder> findByIdAndUser(Long id, User user);

    Page<CustomerOrder> findAllByOrderByOrderDateDesc(Pageable pageable);

    Page<CustomerOrder> findByOrderStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.orderStatus = :status")
    long countByOrderStatus(@Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM CustomerOrder o WHERE o.orderStatus NOT IN ('CANCELLED')")
    BigDecimal calculateTotalSales();

    List<CustomerOrder> findTop5ByOrderByOrderDateDesc();
}
