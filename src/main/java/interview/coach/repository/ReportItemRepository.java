package interview.coach.repository;

import interview.coach.domain.entity.ReportItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportItemRepository extends JpaRepository<ReportItem, UUID> {

    List<ReportItem> findByReport_IdOrderBySortOrderAsc(UUID reportId);

    void deleteByReport_Id(UUID reportId);
}
