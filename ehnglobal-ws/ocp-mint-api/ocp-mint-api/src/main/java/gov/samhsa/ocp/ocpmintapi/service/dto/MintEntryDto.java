package gov.samhsa.ocp.ocpmintapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MintEntryDto {
    private List<String> key;
    private List<String> value;

    @Override
    public String toString() {
        return "EntryDto{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
