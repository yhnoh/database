FROM redis:7.2.4-alpine

EXPOSE 26379

COPY sentinel/sentinel.conf /etc/redis/sentinel.conf

RUN chmod 777 /etc/redis/sentinel.conf
#  \
#     && chown redis:redis /etc/redis/sentinel.conf

COPY sentinel/sentinel-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/sentinel-entrypoint.sh

ENTRYPOINT ["sentinel-entrypoint.sh"]


