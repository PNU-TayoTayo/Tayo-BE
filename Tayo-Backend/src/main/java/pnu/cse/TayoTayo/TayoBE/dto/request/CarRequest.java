package pnu.cse.TayoTayo.TayoBE.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class CarRequest {

    @Getter
    @Setter
    public static class sharingRequest{
        private double carID;
        private Long lenderID;
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

    @Getter
    @Setter
    public static class modifyCarRequest{
        private double carID;
        private List<String> dateList;
        private String sharingLocation;
        private String sharingLocationAddress;
        private double sharingLatitude;
        private double sharingLongitude;
        private boolean sharingAvailable;
        private int sharingPrice;
    }

    @Getter
    @Setter
    public static class modifySharingRequest{
        private double carID;
    }

    @Getter
    @Setter
    public static class payRequest{
        private double carID;
        private Long lenderID;
        private int sharingPrice;
    }
}
