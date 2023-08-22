package pnu.cse.TayoTayo.TayoBE.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectState {

    private ConcurrentHashMap<String,Long> userSessions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long,Long> userRooms = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long,Integer> userCount = new ConcurrentHashMap<>();

    // 이건 최초 웹소켓 연결시에 userSession 저장용
    public void addUserSession(String userSession, Long userId){
        userSessions.put(userSession,userId);
    }

    // DISCONNECT시에 usersession으로부터 userId 얻어서 userRooms랑 userCount 삭제용
    public Long getUserIdFromUserSession(String userSession){
        if(userSessions.containsKey(userSession)){
            return userSessions.get(userSession);
        }
        return -1L;
    }

    // 방 입장시에 방에서 방으로 옮긴상황이면 
    // 원래는 방 나가기 api 호출하고 방 옮겨야 함
    // userRoom에 roomId 바꿔주고, count에는 이전방 -1 이후 방 +1
    public void addUserRoom(Long userId , Long roomId){

        if(userRooms.containsKey(userId)){// 어딘가 존재할 때 (방에서 방 옮길때)
            if(userRooms.get(userId) == roomId){ // 같은 방에서 같은방으로
                return;
            }else{ // 다른 방으로 이동시
                // 이전 방 인원감소
                Integer beforeRoomCount = userCount.get(userRooms.get(userId));
                userCount.put(userRooms.get(userId),beforeRoomCount-1);
                
                // 새로운 방 입장하고 인원수 증가
                userRooms.put(userId,roomId);
                userCount.put(roomId, userCount.getOrDefault(roomId,0)+1);
            }
        }else{ // 첫입장시
            userRooms.put(userId,roomId);
            userCount.put(roomId, userCount.getOrDefault(roomId,0)+1);
        }
        
    }

    // 실시간 채팅이나 알림시에 isRead 상태처리를 위한 메소드
    // 이게 2이면 둘다 현재 같은 방에 있으므로 바로 메시지/알림을 읽음 처리로 하면됨
     public int getUserCount(Long roomId){
        System.out.println(userCount.get(roomId));
        return userCount.get(roomId);
    }

    public Long getUserRoom(Long userId){

        if(userRooms.contains(userId)){
            return userRooms.get(userId);
        }
        return -1L;
    }

    // DISCONNECT시에 usersession 제거용
    public void deleteUserSession(String userSession){
        userSessions.remove(userSession);
    }

    // DISCONNECT시에 userRoom, userCount 제거용
    public void deleteUserRoom(Long userId){

        if(userRooms.containsKey(userId)){ // 유저가 어떤 방에 포함되어 있다가 나감
            if(userCount.get(userRooms.get(userId)) == 1){ // 한명만 남은 방이면
                userCount.remove(userRooms.get(userId)); // 방 삭제
            }else{ // 두명 방이면
                userCount.put(userRooms.get(userId),1); // 방인원수 1로
            }
            userRooms.remove(userId); // userRoom에서도 userId 삭제
        }
    }

}
