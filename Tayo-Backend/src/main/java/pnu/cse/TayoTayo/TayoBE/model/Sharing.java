package pnu.cse.TayoTayo.TayoBE.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Sharing {
    private double sharingID;
    private double carID;
    private Long lenderID;
    private Long borrowerID;
    private int sharingPrice;
    private String sharingDate;
    private String sharingLocation;
    private String sharingStatus;
}
