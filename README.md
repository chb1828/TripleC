# TripleC (Cryptocurrency Climber Chat)

> Upbit WebSocket 기반 실시간 가상화폐 데이터를 수집하고, InfluxDB + Grafana로 시각화하며 LocalAI를 활용해 시장 흐름을 분석하는 Spring Boot + Kotlin 프로젝트
실시간 가상화폐 데이터 수집 · 시계열 저장 · 시각화 및 AI 기반 분석을 수행하는 개인 프로젝트입니다.

본 프로젝트는 실시간 스트리밍 데이터 처리, 시계열 데이터베이스 설계,  
그리고 **Self-hosted LocalAI 기반 분석 파이프라인** 구축을 목표로 합니다.

---

## 프로젝트 개요

- Upbit WebSocket을 통해 가상화폐 실시간 데이터를 수집
- 수집된 데이터를 InfluxDB에 시계열 데이터로 저장
- Grafana를 통해 시장 흐름 및 핵심 지표를 실시간 시각화
- LocalAI 기반 Analyzer를 통해 시장 상태를 정성적으로 분석
- 스트리밍 수집과 분석을 분리한 확장 가능한 아키텍처 지향

---

## 아키텍처

<img width="542" height="293" alt="image" src="https://github.com/user-attachments/assets/d03596c4-236f-49b4-b3ad-2a2e55bc4517" />

---

## 모듈 구성

### 1. Collector

실시간 가상화폐 데이터를 수집하고 시계열 데이터베이스에 저장하는 모듈입니다.

**주요 기능**
- Upbit WebSocket 연결 및 메시지 수신
- 현재가, 체결, 호가, 캔들 데이터 처리
- InfluxDB 시계열 데이터 저장
- 메시지 버퍼링 및 배치 Flush 구조
- WebSocket 연결 제한 대응 (초당 5개 제한)

**기술 포인트**
- `SimpleTokenBucketRateLimiter`를 통한 연결 제한 제어
- `AbstractMessageHandler` 기반 메시지 처리 추상화
- 멀티 스레드 환경에서 안전한 메시지 버퍼링 구조 설계

---

### 2. Analyzer (LocalAI)

수집된 시계열 데이터를 기반으로 **LocalAI를 활용한 시장 분석**을 수행하는 모듈입니다.

**주요 기능**
- Collector로부터 Flush된 배치 데이터 수신
- 가격 변동률, 거래량, 호가 변화 등 핵심 지표 요약
- 요약된 컨텍스트를 LocalAI에 전달하여 시장 상태 분석
- 급등 가능성에 대한 정성적 분석 결과 생성

**기술 포인트**
- 규칙 기반 판단이 아닌 **LLM 기반 분석 구조**
- 외부 API 의존성 없이 Self-hosted LocalAI 환경 구성
- Analyzer는 판단 주체가 아닌 **컨텍스트 생성 및 결과 해석 레이어**로 설계
- InfluxDB 재조회 결과를 함께 전달하여 분석 신뢰도 보강

---

## 시계열 데이터 & 시각화

### InfluxDB
- 실시간 가상화폐 데이터를 시계열로 저장
- 가격, 거래량, 호가 변화 등 시간 기반 분석에 최적화
- Analyzer 및 Grafana에서 공통 데이터 소스로 사용

### Grafana
- InfluxDB를 데이터 소스로 연동
- 실시간 가격 흐름, 거래량, 변동률 시각화
- 운영 관점에서 시장 상태를 직관적으로 파악 가능

---

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Streaming**: Spring WebSocket
- **AI Analysis**: LocalAI (Self-hosted LLM)
- **Database**
    - InfluxDB (시계열 데이터)
    - RDB (코인 메타데이터)
- **Monitoring / Visualization**
    - Grafana
- **Infra**: AWS, Docker Compose
- **Build**: Gradle Kotlin DSL

---

## 설계 포인트

- 실시간 대용량 데이터 처리를 고려한 비동기 수집 구조
- Collector / Analyzer 분리를 통한 책임 명확화
- AI 분석 로직을 코드에서 분리하여 유연성 확보
- 시계열 데이터 단일 책임 관리 (InfluxDB)
- Grafana 기반 운영·분석 통합 시각화
- Rate Limit 및 장애 상황을 고려한 실전형 설계

---

## 기대 효과

- 실시간 가상화폐 시장 흐름에 대한 직관적인 모니터링
- 규칙 기반이 아닌 AI 기반 분석을 통한 확장성 확보
- 분석 기준 변경 시 코드 수정 최소화
- 스트리밍·시계열·AI 분석을 결합한 백엔드 설계 경험

---

## 향후 계획

- Analyzer 결과 기반 알림 시스템 연동
- LocalAI 모델 교체 및 프롬프트 실험 자동화
- Grafana 대시보드 고도화 (급등 패턴 시각화)
- OpenSearch 연계 분석 데이터 조회

