package pnu.cse.TayoTayo.TayoBE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatMessageEntity;

public interface ChatMessageRespository extends JpaRepository<ChatMessageEntity, Long> {
}
