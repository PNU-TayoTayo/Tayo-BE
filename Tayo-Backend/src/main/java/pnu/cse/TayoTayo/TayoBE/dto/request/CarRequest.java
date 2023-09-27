package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class CarRequest {

    @Getter
    @Setter
    public static class sharingRequest{
        private double carID;
        private double lenderID;
        private double borrowerID;
        private int sharingPrice;
        private String sharingDate;
        private String sharingLocation;
        private String sharingStatus;

        public double generateSharingIDFromCarID() {
            // carID를 기반으로 UUID 생성
            UUID uuid = UUID.nameUUIDFromBytes(String.valueOf(carID).getBytes());
            // UUID를 long으로 변환하여 sharingID로 설정
            return uuid.getMostSignificantBits();
        }
    }

}
