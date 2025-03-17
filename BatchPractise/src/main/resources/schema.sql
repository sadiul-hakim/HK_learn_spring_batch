create table if not exists salary
(
    id         int primary key auto_increment,
    name       varchar,
    department varchar,
    salary     FLOAT8
);

create table if not exists pjm_day_ahead_price
(
    id                     int auto_increment primary key,
    datetime_beginning_ept varchar(20),
    pnode_name             varchar(15),
    system_energy_price_da float4,
    total_lmp_da           float4,
    congestion_price_da    float4,
    marginal_loss_price_da float4,
    total_da float
);