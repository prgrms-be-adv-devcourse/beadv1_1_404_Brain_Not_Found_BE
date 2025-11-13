#!/usr/bin/env bash
source "$(dirname "$0")/00-env.sh"

echo "Initializing Kafka topics..."

for TOPIC in "${!DEFAULT_TOPICS[@]}"; do
  PARTITIONS=${DEFAULT_TOPICS[$TOPIC]}
  log "Creating topic '$TOPIC' with $PARTITIONS partitions and $DEFAULT_REPLICATION replication"
  $KAFKA_BIN/kafka-topics.sh \
    --bootstrap-server "$BROKER" \
    --create \
    --topic "$TOPIC" \
    --partitions "$PARTITIONS" \
    --replication-factor "$DEFAULT_REPLICATION" \
    --if-not-exists \
    | tee -a "$LOG_DIR/topic.log"
done

echo "Kafka topics initialization completed."
echo $KAFKA_BIN/kafka-topics.sh --list --bootstrap-server "$BROKER"