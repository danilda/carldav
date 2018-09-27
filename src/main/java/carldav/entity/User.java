/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package carldav.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(name = "user_email", columnNames = "email")})
public class User {

    private Long id;
    private String password;
    private String email;
    private boolean locked;
    private Set<CollectionItem> collections;
    private String role;
    private Set<User> managers = new HashSet<>();

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    @Column(name = "email", nullable = false)
    public String getEmail() {
        return email;
    }

    public boolean isLocked() {
        return locked;
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public Set<CollectionItem> getCollections() {
        return collections;
    }

    public void setCollections(Set<CollectionItem> collections) {
        this.collections = collections;
    }

    public String getRole() {
        return role;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name = "USER_MANAGER",
            joinColumns = {@JoinColumn(name = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "MANAGER_ID")})
    public Set<User> getManagers() {
        return managers;
    }

    public void setManagers(Set<User> managers) {
        this.managers = managers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(" - ").append(email).append(", managers : [");
        managers.forEach(manager ->
                sb.append("\n \t").append(manager.getId()).append(" - ").append(manager.email)
        );
        return sb.append("\n]").toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new EqualsBuilder()
                .append(locked, user.locked)
                .append(id, user.id)
                .append(password, user.password)
                .append(email, user.email)
                .append(role, user.role)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(email)
                .append(locked)
                .append(role)
                .toHashCode();
    }
}