package team.waitingcatch.app.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import team.waitingcatch.app.restaurant.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
