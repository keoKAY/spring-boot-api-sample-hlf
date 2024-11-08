package anuznomii.lol.apihyperledgerfabricspring.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CAEnrollmentRequest {
    private String username; 
    private String secret; 
    private String affliation; // org1.department1
    private String orgName; 
    private String registrarUsername; // we can hide this in prod
    private String type ; // peer  | client | admin ! 
    @Builder.Default
    private Boolean genSecret = true; 

}
