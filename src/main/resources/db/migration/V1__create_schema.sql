create table rescue_centers (
    id bigserial primary key,
    code varchar(255) not null unique,
    name varchar(255) not null,
    city varchar(255) not null
);

create table rescue_cases (
    id bigserial primary key,
    case_code varchar(255) not null unique,
    rescue_date date not null,
    rescue_location varchar(100) not null,
    status varchar(50) not null,
    rescue_center_id bigint not null,

    constraint fk_rescue_case_center
        foreign key (rescue_center_id)
        references rescue_centers(id),

    constraint chk_case_status
        check (status in (
            'ADMITTED',
            'UNDER_EVALUATION',
            'IN_REHABILITATION',
            'READY_FOR_RELEASE',
            'RELEASED',
            'CLOSED'
        ))
);

create table animals (
    id bigserial primary key,
    animal_code varchar(255) not null unique,
    common_name varchar(255) not null,
    scientific_name varchar(255) not null,
    sex varchar(10) not null,
    rescue_case_id bigint not null unique,

    constraint fk_animal_case
        foreign key (rescue_case_id)
        references rescue_cases(id)
);

create table medical_records (
    id bigserial primary key,
    animal_id bigint not null unique,
    initial_weight numeric(10, 2) not null,
    initial_condition varchar(255) not null,
    injuries text,
    observations text,

    constraint fk_medical_record_animal
        foreign key (animal_id)
        references animals(id)
);

create table specialists (
    id bigserial primary key,
    professional_code varchar(60) not null unique,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    email varchar(100) not null unique,
    active boolean
);

create table expertise (
    id bigserial primary key,
    name varchar(100) not null unique
);

create table specialist_expertise (
    specialist_id bigint not null,
    expertise_id bigint not null,

    constraint fk_specialist
        foreign key (specialist_id)
        references specialists(id),

    constraint fk_expertise
        foreign key (expertise_id)
        references expertise(id),

    primary key (specialist_id, expertise_id)
);

create table treatments (
    id bigserial primary key,
    animal_id bigint not null,
    specialist_id bigint not null,
    performed_at timestamp not null,
    description varchar(255) not null,

    constraint fk_treatment_animal
        foreign key (animal_id)
        references animals(id),

    constraint fk_treatment_specialist
        foreign key (specialist_id)
        references specialists(id)
);

create index idx_rescue_cases_rescue_center_id
    on rescue_cases(rescue_center_id);

create index idx_rescue_cases_rescue_date
    on rescue_cases(rescue_date);

create index idx_rescue_cases_status
    on rescue_cases(status);

create index idx_animals_rescue_case_id
    on animals(rescue_case_id);

create index idx_medical_records_animal_id
    on medical_records(animal_id);

create index idx_specialist_expertise_expertise_id
    on specialist_expertise(expertise_id);

create index idx_treatments_animal_id
    on treatments(animal_id);

create index idx_treatments_specialist_id
    on treatments(specialist_id);

create index idx_treatments_performed_at
    on treatments(performed_at);