package pnu.cse.TayoTayo.TayoBE.dto.response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pnu.cse.TayoTayo.TayoBE.model.Member;

import java.util.List;

@Getter
@AllArgsConstructor
public class CarResponse {

    private List<CarDetail> carDetailList;

    @Getter
    @Setter
    @Builder
    public static class CarDetail {
        private Double carID;
        private Long ownerID;
        private String model;
        private String engine;
        private String deliveryDate;
        private int drivingRecord;
        private String inspectionRecord;
        private String[] dateList;
        private String sharingLocation;
        private String sharingLocationAddress;
        private Double sharingLatitude;
        private Double sharingLongitude;
        private boolean sharingAvailable;
        private int sharingPrice;
        private int sharingRating;

        public static CarDetail fromJson(JsonElement json) {
            Gson gson = new Gson();
            return gson.fromJson(json, CarDetail.class);
        }
    }

}
