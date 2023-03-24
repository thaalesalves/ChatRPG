package es.thalesalv.chatrpg.adapters.data.db.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lorebook")
public class LorebookEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "lorebook_name")
    private String name;

    @Column(name = "lorebook_description", length = 2000)
    private String description;

    @Column(name = "owner_discord_id", nullable = false)
    private String owner;

    @Column(name = "edit_permission_discord_ids")
    private String editPermissions;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "entry_description")
    @OneToMany(mappedBy = "lorebook", fetch = FetchType.EAGER)
    private List<LorebookRegexEntity> entries;
}
