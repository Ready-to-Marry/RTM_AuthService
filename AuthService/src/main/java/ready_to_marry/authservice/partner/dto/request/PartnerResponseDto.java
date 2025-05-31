package ready_to_marry.authservice.partner.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PartnerResponseDto {
    private String name;

    private String companyName;

    private String address;

    private String phone;

    private String companyNum;

    private String businessNum;
}
