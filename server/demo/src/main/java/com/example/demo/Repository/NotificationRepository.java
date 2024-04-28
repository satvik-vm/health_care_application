package com.example.demo.Repository;

import com.example.demo.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findBySender(String sender);
    List<Notification> findByDate(String date);
}
