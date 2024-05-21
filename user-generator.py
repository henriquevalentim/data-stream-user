import json
import random
import time

from faker import Faker
from confluent_kafka import SerializingProducer
from datetime import datetime, timezone

fake = Faker()

def generate_user():
    user = fake.simple_profile()
    genre = random.choice(['M', 'F'])
    first_name = fake.first_name_male() if genre == "M" else fake.first_name_female()
    last_name = fake.last_name()

    return {
        "id": fake.uuid4(),
        "name": f"{first_name} {last_name}",
        "email": f"{first_name.lower()}.{last_name.lower()}@{fake.domain_name()}",
        "genre": genre,
        'registerDate': datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.%f%z')
    }

def delivery_report(err, msg):
    if err is not None:
        print(f'Message delivery failed: {err}')
    else:
        print(f"Message delivered to {msg.topic()} [{msg.partition()}]")

def main():
    topic = 'user'
    producer = SerializingProducer({
        'bootstrap.servers': 'localhost:9092'
    })

    curr_time = datetime.now()

    while (datetime.now() - curr_time).seconds < 120:
        try:
            user = generate_user()

            # print(user)

            producer.produce(topic,
                             key=user['id'],
                             value=json.dumps(user),
                             on_delivery=delivery_report)
            producer.poll(0)
            

            # time.sleep(2)
        except BufferError:
            print("Buffer full! Waiting...")
            time.sleep(1)
        except Exception as e:
            print(e)

    producer.flush()

if __name__ == "__main__":
    main()