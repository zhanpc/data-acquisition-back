#! /bin/bash
if [ ${RUN_MODE}x = "test"x ]
then
    echo "NOTICE: you are running this application in test mode!"
    java -Djava.security.egd=file:/dev/./urandom -jar /app.jar -Duser.timezone=GMT+8 --spring.profiles.active=test
elif [ ${RUN_MODE}x = "prod"x ]
then
    echo "NOTICE: you are running this application in prod mode!"
    java -Djava.security.egd=file:/dev/./urandom -jar /app.jar -Duser.timezone=GMT+8 --spring.profiles.active=prod
else
    echo "NOTICE: you are running this application in dev mode!"
    java -Djava.security.egd=file:/dev/./urandom -jar /app.jar -Duser.timezone=GMT+8 --spring.profiles.active=dev
fi
