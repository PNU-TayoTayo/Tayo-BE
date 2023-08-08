package pnu.cse.TayoTayo.TayoBE.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pnu.cse.TayoTayo.TayoBE.dto.request.MemberRequest;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyVCResponse {

    private String userName;

    private List<VerifiableCredential> vc;

    @Getter @Setter @Builder
    public static class VerifiableCredential{

        private String referent;

        private String name;
        private String carModel;
        private String carNumber;
        private String carFuel;
        private String carDeliveryDate;
        private String inspectionRecord;
        private String drivingRecord;
    }

}
