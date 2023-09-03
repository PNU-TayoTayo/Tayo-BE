package pnu.cse.TayoTayo.TayoBE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pnu.cse.TayoTayo.TayoBE.model.entity.ChatRoomEntity;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // => 해당 from_member_id 또는 to_member_id에 해당 userId가 속해있는 모든 chatRoomEntity을
    // updateAt 기준으로 최근 꺼부터 들고오기
    @Query("SELECT cr FROM ChatRoomEntity cr WHERE cr.fromMember.id = :userId OR cr.toMember.id = :userId ORDER BY cr.updatedAt DESC")
    List<ChatRoomEntity> findByFromMemberOrToMemberOrderByCreatedAtDesc(@Param("userId") Long userId);


}
