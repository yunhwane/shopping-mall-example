-- Spring Modulith JPA event publication registry. Generated from the framework's
-- entity mapping (DefaultJpaEventPublication) so Hibernate `validate` passes — we
-- previously relied on H2 auto-DDL for this. See ADR-0004.
create table event_publication (
    id                     uuid                     not null,
    listener_id            varchar(255)             not null,
    event_type             varchar(255)             not null,
    serialized_event       varchar(255)             not null,
    publication_date       timestamp(6) with time zone not null,
    completion_date        timestamp(6) with time zone,
    last_resubmission_date timestamp(6) with time zone,
    completion_attempts    integer                  not null,
    status                 varchar(255),
    primary key (id)
);
