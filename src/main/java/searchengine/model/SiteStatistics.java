package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "site_statistics")
public class SiteStatistics {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "url")
    private String url;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private String status;

    @Column(name = "status_time")
    private Timestamp statusTime;

    @Column(name = "error")
    private String error;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "lemmas")
    private Integer lemmas;
}
