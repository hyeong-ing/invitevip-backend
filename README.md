# 🔒 Invite and Management 🔑

<br/>

<p align="center">

  <br/>
  이전 프로젝트에서 회원가입과 로그인을 구현했지만, 로그아웃이나 인증 상태 유지, 권한 처리까지 깊게 다루지 못했습니다. <br/>
  그리고 DB도 H2 위주로만 사용해서 실제 DB를 설계하고 관리하는 경험이 부족하다고 느꼈습니다.<br/>
  그래서 이번 프로젝트에서는 Keycloak을 활용한 인증, Spring Security 기반의 권한 처리,<br/>
  MySQL을 사용한 고객/관리자 데이터 관리까지 경험하는 것을 목표로 했습니다. <br/>
  <br/>

  <br/>

  <img width="800" height="450" alt="image" src="https://github.com/user-attachments/assets/25ed9b61-e432-438e-a6c4-a49c78ee17c9" />

</p>

<br/>
<br/>
<br/>

### 🔶 프로젝트 관련 링크

+ [Blog (프로젝트 기록)](https://post-this.tistory.com/category/%F0%9F%92%BB%20%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8/%F0%9F%90%A0%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85%20%ED%8E%98%EC%9D%B4%EC%A7%80%F0%9F%90%A0)
+ Youtube (동작화면)
+ [Figma (다이어그램)](https://www.figma.com/board/pcWxgbFCWQUnnIW3W1hrZi/%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8?node-id=0-1&t=NPUJ2hrnFEb7meeQ-1)


<br/>
<br/>


### 🔶 프로젝트 설명

<br/>

<p align="center">

  <img width="800" height="500" alt="스크린샷 2026-07-03 오후 8 56 27" src="https://github.com/user-attachments/assets/89561780-116a-4091-9e56-ec9c44c8152f" />

</p>

<br/>

+ 사용자가 입력한 정보로 일반 회원가입을 진행합니다. 
+ 아이디와 이메일 중복 확인을 통해 이미 등록된 회원인지 검증합니다.
+ H2 데이터베이스로 회원 정보를 저장합니다.
+ BCryptPasswordEncoder를 사용해 비밀번호를 암호화하여 저장합니다.
+ 카카오와 네이버 OAuth API를 연동해 소셜 로그인 흐름을 구현했습니다.

<br/><br/> 

### 🔶 기술 스택 & 라이브러리
+ 백엔드 : Java 17, Spring Boot
+ 데이터베이스 : MySQL
+ 검색 엔진 : Elasticsearch, Spring Data Elasticsearch
+ 보안 : Spring Security, OAuth2 Resource Server, JWT, Keycloak
+ 관리자 계정 연동 : Keycloak Admin Client
  
<br/><br/>

### 🔶 프로젝트 목표
+ DB에 저장된 초대코드를 조회하고 해당 고객 등급별 안내 페이지로 이동하는 흐룸 구현하기.
+ MySQL과 JPA로 고객, 관리자, 권한 데이터를 관리하는 실제 DB 기반 프로젝트 경험하기.
+ Elasticsearch를 활용한 고객 검색 기능 구현하기.
+ Keycloak과 Spring Security를 활용한 로그인/로그아웃 및 권한 기반 접근 제어 경험하기.
+ Docker를 사용해 Keycloak, Elasticsearch 같은 외부 시스템을 실행하고 백엔드와 연동하는 흐름 이해하기.

<br/><br/>

### 🔶 핵심 로직
1) 초대코드 검증 및 등급별 페이지 이동 <br/>
사용자가 4자리 초대코드를 입력하면 백엔드에서 DB에 저장된 초대 코드와 일치하는 고객을 조회합니다.

+ 초대코드는 InviteCode 값 객체를 통해 숫자 4자리인지 검증합니다.
+ 유효한 코드라면 고객 정보를 반환합니다.
+ 그리고 프론트엔드에서 고객 등급에 따라 VIP, VVIP, DIAMOND 페이지로 이동합니다.

```java
public Optional<CustomerResponse> findByCode(String code) {
    if (!InviteCode.isValid(code)) {
        return Optional.empty();
    }

    return customerRepository.findByInviteCode(InviteCode.of(code))
            .map(customerService::toResponse);
}
```
```java
const grade = (data.grade || "").trim().toUpperCase();

if (grade === "VIP") navigate("/vip");
else if (grade === "VVIP") navigate("/vvip");
else if (grade === "DIAMOND") navigate("/diamond");
```

<br/><br/>

----

2) 고객 등록∙수정 시 초대코드 중복 방지 <br/>
고객을 등록하거나 수정할 때 동일한 초대코드가 저장되지 않도록 검증했습니다.

+ 둥록 시에는 같은 초대코드가 이미 존재하는지 확인합니다.
+ 수정 시에는 자기 자신의 기존 코드만 허용하고 다른 고객이 사용하는 코드만 중복으로 판단합니다.
+ DB에서는 `code`컬럼에 unique 제약 조건을 두어 한 번 더 중복을 방지했습니다.

```java
if (customerRepository.existsByInviteCode(inviteCode)) {
    throw new DuplicateCodeException("초대코드가 중복되었습니다.");
}
```
```java
customerRepository.findByInviteCode(newInviteCode).ifPresent(found -> {
    if (!found.getId().equals(id)) {
        throw new DuplicateCodeException("초대코드가 중복되었습니다.");
    }
});
```

<br/><br/>

----

3) MySQL 저장 후 Elasticsearch 검색 인덱스 함께 반영 <br/>
고객 원본 데이터는 MySQL에 저장하고, 검색에 사용할 데이터는 Elasticsearch 인덱스에도 함께 반영했습니다.

+ 고객 등록 시 MySQL에 고객 정보를 저장합니다.
+ 저장된 고객 정보를 검색용 Entity로 변환해 Elasticsearch에도 저장합니다.
+ 고객 수정∙삭제 시에도 Elasticsearch 데이터를 함께 갱신하거나 삭제해 검색 결과가 최신 상태를 유지하도록 했습니다.

```java
Customer saved = customerRepository.save(customer);

customerSearchRepository.save(
        customerSearchMapper.toSearchEntity(saved));
```
```java
customer.update(
        request.getName(),
        request.getGrade(),
        request.getPhone(),
        newInviteCode,
        request.getNote()
);

customerSearchRepository.save(
        customerSearchMapper.toSearchEntity(customer)
);
```
```java
customerRepository.delete(customer);
customerSearchRepository.deleteById(id);
```

<br/><br/>

----

4) Elasticsearch 기반 고객 검색 및 초성 검색 <br/>
고객명, 등급, 연락처, 초대코드, 메모를 검색할 수 있도록 Elasticsearch를 사용했습니다.

+ name, note는 한글 검색을 고려해 analyzer를 적용했습니다.
+ 고객 이름에서 초성을 추출해 nameChosung 필드에 저장했습니다.
+ 예를 들어 홍길동은 ㅎㄱㄷ으로 저장되어 초성 검색이 가능합니다.
+ 아래 코드는 검색 조건 중 핵심 부분을 발췌한 예시입니다.

```java
public CustomerSearchEntity toSearchEntity(Customer customer) {
    CustomerSearchEntity entity = new CustomerSearchEntity();

    entity.setId(customer.getId());
    entity.setName(customer.getName());
    entity.setGrade(customer.getGrade());
    entity.setPhone(customer.getPhone());
    entity.setCode(customer.getCode());
    entity.setNote(customer.getNote());
    entity.setNameChosung(getChosung(customer.getName()));

    return entity;
}
```
```java
if (c >= '가' && c <= '힣') {
    int uniVal = c - 0xAC00;
    int choIdx = uniVal / 588;
    sb.append(cho[choIdx]);
}
```
```java
@Query("""
{
  "bool": {
    "should": [
      { "match": { "name": "?0" } },
      { "match": { "note": "?0" } },
      { "wildcard": { "grade": { "value": "*?0*" } } },
      { "wildcard": { "phone": { "value": "*?0*" } } },
      { "wildcard": { "code": { "value": "*?0*" } } },
      { "wildcard": { "nameChosung": { "value": "*?0*" } } }
    ],
    "minimum_should_match": 1
  }
}
""")
List<CustomerSearchEntity> searchAll(String keyword);
```

<br/><br/>

----

5) JWT 인증 정보와 DB 권한을 결합한 권한 제어 <br/>
Keycloak에서 발급받은 JWT를 Spring Security Resource Server가 검증하고 <br/>
DB에 저장된 관리자 권한을 함께 읽어 API 접근 권한을 제어했습니다.

+ JWT의 realm role과 DB에 저장된 관리자 role을 Spring Security 권한 형식인 ROLE_ADMIN, ROLE_SUPER_ADMIN으로 사용합니다.
+ DB의 관리자 권한을 조회해 CUSTOMER_SEARCH, CUSTOMER_ADD, CUSTOMER_EDIT, CUSTOMER_DELETE 권한으로 변환합니다.
+ Controller에서는 @PreAuthorize를 사용해 API별 접근 권한을 분리했습니다.

```java
authorities.addAll(extractRealmRoles(jwt));
authorities.addAll(adminAuthorityService.loadAuthorities(jwt));
```
```java
if (admin.getRole() != null) {
    authorities.add(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()));
}

for (AdminPermission adminPermission : admin.getAdminPermissions()) {
    Permission permission = adminPermission.getPermission();

    if (permission == null || permission.getCode() == null) {
        continue;
    }

    authorities.add(new SimpleGrantedAuthority(
            permission.getCode().trim().toUpperCase()
    ));
}
```


<br/><br/><br/>


### 🔶 문제 해결

### [ 관리자 권한 다대다 관계 설계와 중복 저장 방지 ] <br/>

1) 문제 발생 <br/>
+ 한 명의 관리자가 여러 권한을 가질 수 있고 하나의 권한도 여러 관리자에게 부여될 수 있는 구조가 필요했습니다.
+ 관계형 DB에서는 다대다 관계를 직접 관리하기 어렵고 권한 추가, 삭제 흐름도 명확하게 보이지 않았습니다.
+ 또한 같은 관리자에게 같은 권한이 중복 저장될 수 있는 문제가 있었습니다.

<br/><br/>

2) 원인 파악 <br/>
+ Admin과 Permission은 다대다 관계이지만, @ManyToMany를 직접 사용하면 중간 테이블을 객체로 다루기 어렵습니다.
+ 권한 연결 자체를 명확히 관리하고, 추후 필드 확장 가능성도 고려해야 했습니다.

<br/><br/>

3) 문제 해결 <br/>
+ AdminPermission 중간 엔티티를 만들어 관리자와 권한의 연결을 직접 관리했습니다.
+ admin_id, permission_id 조합에 unique 제약을 두어 중복 저장을 방지했습니다.
+ orphanRemoval = true를 사용해 권한 목록에서 제거된 연결 데이터가 DB에서도 삭제되도록 처리했습니다.
  
```

```

<br/><br/>

### [ MySQL과 Elasticsearch 검색 인덱스 불일치 문제 ] <br/>

1) 문제 발생 <br/>
+ 고객 원본 데이터는 MySQL에 저장하고, 검색 데이터는 Elasticsearch에 저장했습니다.
+ 고객 정보가 변경되었을 때 Elasticsearch 인덱스가 함께 갱신되지 않으면 검색 결과가 실제 DB와 달라질 수 있었습니다.

<br/><br/>

2) 원인 파악 <br/>
+ MySQL과 Elasticsearch는 서로 다른 저장소이기 때문에, MySQL 저장만으로 Elasticsearch 데이터가 자동 변경되지 않습니다.
+ 고객 등록, 수정, 삭제 시점에 검색 인덱스도 함께 반영해야 했습니다.

<br/><br/>

3) 문제 해결 <br/>
+ 고객 등록 / 수정 / 삭제 시 Elasticsearch 인덱스도 함께 저장하거나 삭제하도록 처리했습니다.
+ MySQL 기준 고객 데이터를 Elasticsearch에 다시 반영할 수 있는 수동 동기화 API도 추가했습니다.
  
```

```

<br/><br/>

### [ MySQL과 keycloak 관리자 계정 불일치 문제 ] <br/>

1) 문제 발생 <br/>
+ 관리자 정보는 MySQL에 저장하고, 실제 로그인 계정은 Keycloak에 생성했습니다.
+ 둘 중 하나만 반영되면 DB에는 있지만 로그인할 수 없거나, 로그인은 되지만 서비스 권한을 조회할 수 없는 문제가 생길 수 있었습니다.

<br/><br/>

2) 원인 파악 <br/>
+ MySQL과 Keycloak은 서로 다른 시스템이기 때문에 하나의 트랜잭션으로 자동 처리되지 않습니다.
+ Keycloak 계정과 MySQL 관리자 데이터를 연결할 식별값도 필요했습니다.

<br/><br/>

3) 문제 해결 <br/>
+ 관리자 생성 시 saveAndFlush()로 DB insert와 제약 조건 검사를 먼저 실행한 뒤 Keycloak 사용자 생성을 진행했습니다.
+ 생성된 Keycloak 사용자 id를 admin.keycloakId에 저장해 MySQL 관리자와 Keycloak 계정을 연결했습니다.
+ 관리자 수정 / 삭제 시에도 MySQL 데이터와 Keycloak 사용자 계정을 함께 반영하도록 처리했습니다.
  
```

```

<br/><br/>






