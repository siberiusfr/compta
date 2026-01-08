package tn.cyberious.compta.oauth2.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomUserDetails implements UserDetails {

  private static final long serialVersionUID = 1L;

  private final UUID id;
  private final String username;
  private final String password;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final boolean enabled;
  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final List<String> roleNames;

  @JsonIgnore
  private transient UsersRecord userRecord;

  @JsonCreator
  public CustomUserDetails(
      @JsonProperty("id") UUID id,
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("email") String email,
      @JsonProperty("firstName") String firstName,
      @JsonProperty("lastName") String lastName,
      @JsonProperty("enabled") boolean enabled,
      @JsonProperty("accountNonExpired") boolean accountNonExpired,
      @JsonProperty("accountNonLocked") boolean accountNonLocked,
      @JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
      @JsonProperty("roleNames") List<String> roleNames) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.enabled = enabled;
    this.accountNonExpired = accountNonExpired;
    this.accountNonLocked = accountNonLocked;
    this.credentialsNonExpired = credentialsNonExpired;
    this.roleNames = roleNames;
  }

  public CustomUserDetails(UsersRecord userRecord, List<String> roleNames) {
    this.userRecord = userRecord;
    this.id = userRecord.getId();
    this.username = userRecord.getUsername();
    this.password = userRecord.getPassword();
    this.email = userRecord.getEmail();
    this.firstName = userRecord.getFirstName();
    this.lastName = userRecord.getLastName();
    this.enabled = userRecord.getEnabled();
    this.accountNonExpired = userRecord.getAccountNonExpired();
    this.accountNonLocked = userRecord.getAccountNonLocked();
    this.credentialsNonExpired = userRecord.getCredentialsNonExpired();
    this.roleNames = roleNames;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roleNames.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public List<String> getRoleNames() {
    return roleNames;
  }

  @JsonIgnore
  public UsersRecord getUserRecord() {
    return userRecord;
  }
}
