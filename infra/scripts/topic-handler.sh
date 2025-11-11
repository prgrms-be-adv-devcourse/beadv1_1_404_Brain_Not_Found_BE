#!/usr/bin/env bash
source "$(dirname "$0")/00-env.sh"

usage() {
  echo "Usage:"
  echo "  $0 create <topic> [partitions] [replication] defaults to $DEFAULT_PARTITIONS partitions and $DEFAULT_REPLICATION replication"
  echo "  $0 delete <topic>"
  echo "  $0 describe <topic>"
  echo "  $0 list"
  echo "  $0 expand <topic> <new_partition_count>"
}

ACTION=$1
TOPIC=$2
PARTITIONS=${3:-$DEFAULT_PARTITIONS}
REPLICATION=${3:-$DEFAULT_REPLICATION}

case "$ACTION" in
  create)
    log "Creating topic '$TOPIC' ($PARTITIONS partitions, $REPLICATION replicas)"
    $KAFKA_BIN/kafka-topics.sh \
      --bootstrap-server "$BROKER" \
      --create \
      --topic "$TOPIC" \
      --partitions "$PARTITIONS" \
      --replication-factor "$REPLICATION" \
      --if-not-exists \
      | tee -a "$LOG_DIR/topic.log"
    ;;
  delete)
    log "Deleting topic '$TOPIC'"
    $KAFKA_BIN/kafka-topics.sh \
      --bootstrap-server "$BROKER" \
      --delete \
      --topic "$TOPIC" \
      | tee -a "$LOG_DIR/topic.log"
    ;;
  describe)
    log "Describing topic '$TOPIC'"
    $KAFKA_BIN/kafka-topics.sh \
      --bootstrap-server "$BROKER" \
      --describe \
      --topic "$TOPIC" \
    ;;
  list)
    log "Listing all topics"
    $KAFKA_BIN/kafka-topics.sh --list --bootstrap-server "$BROKER"
    ;;
  expand)
    NEW_PARTITION_COUNT=$3
    log "Expanding topic '$TOPIC' to $NEW_PARTITION_COUNT partitions"
    $KAFKA_BIN/kafka-topics.sh \
      --bootstrap-server "$BROKER" \
      --alter \
      --topic "$TOPIC" \
      --partitions "$NEW_PARTITION_COUNT"\
      | tee -a "$LOG_DIR/topic.log"
    ;;
  *)
    usage
    exit 1
    ;;
esac