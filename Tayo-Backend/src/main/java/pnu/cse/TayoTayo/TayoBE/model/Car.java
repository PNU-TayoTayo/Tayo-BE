package pnu.cse.TayoTayo.TayoBE.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Car {
    private double carID;
    private double ownerID;
    private String model;
    private String engine;
    private String deliveryDate;
    private int drivingRecord;
    private String inspectionRecord;
    private List<String> dateList;
    private String sharingLocation;
    private String sharingLocationAddress;
    private double sharingLatitude;
    private double sharingLongitude;
    private boolean sharingAvailable;
    private int sharingRating;
}
