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
1) 소셜 로그인 콜백 처리 <br/>
카카오/네이버 로그인 버튼을 클릭하면 사용자는 외부 OAuth 로그인 페이지로 이동합니다.

+ 소셜 로그인 후 전달받은 인가 코드를 Vue 콜백 페이지에서 추출합니다.
+ Spring Boot 백엔드로 전달해 사용자 정보를 받아오는 흐름을 구현했습니다.

```
const params = new URLSearchParams(window.location.search);
const code = params.get("code");

const response = await axios.post("http://localhost:8080/oauth/kakao", {
  code: code
});

localStorage.setItem("displayName", response.data.nickname);
this.$router.replace("/main");
```

<br/><br/>

----

2) 중복 확인 상태 관리 <br/>
회원가입 화면에서 아이디와 이메일 중복 확인을 진행한 뒤 회원가입을 할 수 있도로 구성했습니다.

+ 아이디와 이메일 중복 확인 여부를 상태값으로 관리합니다.
+ 입력값이 변경되면 다시 중복 확인을 하도록 처리했습니다.

```
watch: {
  userId() {
    this.idDuplicate = true;
  },

  email() {
    this.emailDuplicate = true;
  }
}
```
```
if (this.idDuplicate) {
  alert("아이디 중복 확인을 해주세요.");
  return;
}

if (this.emailDuplicate) {
  alert("이메일 중복 확인을 해주세요.");
  return;
}
```

<br/><br/><br/>


### 🔶 문제 해결

### [ 중복 확인 후 입력값 변경 문제 ] <br/>

1) 문제 발생 <br/>
+ 회원가입 화면에서 아이디와 이메일 중복 확인을 통과한 뒤, 해당 값을 변경해보았습니다.
+ 확인 결과 값을 변경해도 기존 중복 확인 결과가 그대로 유지되는 문제가 있었습니다.

<br/><br/>

2) 원인 파악 <br/>
+ 아이디와 이메일 중복 확인 여부를 상태값으로 관리하고 있었습니다.
+ 그러나 입력값이 변경되었을 때 해당 상태값을 다시 초기화하지 않아 발생한 문제였습니다.

<br/><br/>

3) 문제 해결 <br/>
+ Vue의 watch를 사용해 uesrId와 email 값을 변경될 때마다 중복 확인 상태를 초기화했습니다.
+ 이렇게 사용자가 값을 수정하면 다시 중복 확인을 하도록 개선했습니다.
  
```
watch: {
  userId() {
    this.idDuplicate = true;
  },

  email() {
    this.emailDuplicate = true;
  }
}
```

<br/><br/>





