#!/usr/bin/env bash

# Kafka 기본 환경 변수

# 브로커 노드 리스트 ( 현재 단일 노드 환경이므로 주석 처리 )
BROKER="kafka-1:9090"
#BROKER="kafka-1:9090,kafka-2:9090,kafka-3:9090"

# 컨테이너 내부 Kafka CLI binary 위치
KAFKA_BIN="/opt/kafka/bin"

# Kafka 스크립트 로깅 경로
LOG_DIR="/scripts/logs"
mkdir -p $LOG_DIR

# 기본 replication / partition 값
# 현재 단일 노드 환경이므로 1로 설정
DEFAULT_PARTITIONS=1
DEFAULT_REPLICATION=1
#DEFAULT_PARTITIONS=3
#DEFAULT_REPLICATION=3

# 기본 생성 Topic
declare -A DEFAULT_TOPICS=(
  ["user-create-event"]=1
  ["user-create-event.dlq"]=1
  ["deposit-charge-event"]=1
  ["deposit-charge-event.dlq"]=1
  ["order-event"]=1
  ["order-event.dlq"]=1
  ["settlement-event"]=1
  ["settlement-event.dlq"]=1
  ["refund-event"]=1
  ["refund-event.dlq"]=1
  ["inventory-event"]=1
  ["inventory-event.dlq"]=1
)

# 시간 포맷
NOW=$(date '+%Y-%m-%d %H:%M:%S')

# 로그 출력 함수
log() {
  echo "[$NOW] $1"
}