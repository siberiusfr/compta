package tn.cyberious.compta.oauth2.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.cyberious.compta.oauth2.generated.tables.records.UsersRecord;

public class CustomUserDetails implements UserDetails {

  private final UsersRecord userRecord;
  private final List<String> roleNames;

  public CustomUserDetails(UsersRecord userRecord, List<String> roleNames) {
    this.userRecord = userRecord;
    this.roleNames = roleNames;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roleNames
      .stream()
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return userRecord.getPassword();
  }

  @Override
  public String getUsername() {
    return userRecord.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return userRecord.getAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return userRecord.getAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return userRecord.getCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return userRecord.getEnabled();
  }

  public UsersRecord getUserRecord() {
    return userRecord;
  }
}
