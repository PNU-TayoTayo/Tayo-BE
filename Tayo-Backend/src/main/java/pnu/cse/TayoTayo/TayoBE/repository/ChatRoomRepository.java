package pnu.cse.TayoTayo.TayoBE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatRoomEntity;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
}
