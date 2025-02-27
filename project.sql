-- Building is a general entity that has both stores and suppliers
-- Default with location and identifier
CREATE TABLE building(
    id number(5) GENERATED ALWAYS AS IDENTITY,
    location varchar(20) NOT NULL,
    PRIMARY KEY (id)
);

-- A store is a specialized building with no other additional info
CREATE TABLE storeb(
  store_id 	number(5),
  PRIMARY KEY (store_id),
  FOREIGN KEY (store_id)
    REFERENCES building(id)
    ON DELETE CASCADE
);

-- allows us to view the store with store_id and location
CREATE VIEW store AS(
  SELECT * FROM storeb s
  INNER JOIN building b on s.store_id = b.id
);

-- A supplier is a specialized building with supplier name
CREATE TABLE supplierb(
  supplier_id 	  number(5),
  supplier_name	varchar(20),
  PRIMARY KEY (supplier_id),
  FOREIGN KEY (supplier_id)
    REFERENCES building(id)
    ON DELETE CASCADE
);

-- allows us to view the supplier with supplier_id, supplier_name and location
CREATE VIEW supplier AS(
  SELECT * FROM supplierb s
  INNER JOIN building b on s.supplier_id = b.id
);

-- Each generic product has its own id.
CREATE TABLE productlog (
    product_id	  number(5) GENERATED ALWAYS AS IDENTITY,
    product_name  varchar(20) NOT NULL,
    PRIMARY KEY (product_id)
);

-- Each supplier can have the same product, with its own 
-- unit type and price
-- We can identify the origin by its supplier
CREATE TABLE productb(
  product_id	  number(5),
  supplier_id	  number(5),
  price		      numeric(10,4) DEFAULT 0,
  unit_type     varchar(10) DEFAULT 'ea', -- default to each
  PRIMARY KEY (product_id, supplier_id),
  FOREIGN KEY (supplier_id) 
    REFERENCES supplierb
    ON DELETE CASCADE,
  FOREIGN KEY (product_id) 
    REFERENCES productlog
    ON DELETE CASCADE
);

CREATE VIEW product AS(
  SELECT * FROM productb
  NATURAL JOIN productlog
  NATURAL JOIN supplierb
);


-- Each shipment goes to one location at certain time
-- Each of those shipment is assigned a unique id
CREATE TABLE shipmentLog(
  shipment_id   number(5) GENERATED ALWAYS AS IDENTITY,
  to_id         number(5),
  ship_date     timestamp NOT NULL,
  arrive_date   timestamp NOT NULL, -- check arrival date is after ship date
  PRIMARY KEY (shipment_id),
  FOREIGN KEY (to_id) 
    REFERENCES building(id)
    ON DELETE CASCADE,
  CHECK (arrive_date >= ship_date)
);
-- Multiple products can be for a specfic shipment from any supplier
-- We do not need any primary keys
CREATE TABLE product_ship(
  shipment_id number(5),
  product_id number(5),
  supplier_id   number(5),
  qty           numeric(10,4),
  FOREIGN KEY (product_id, supplier_id)
    REFERENCES productb
    ON DELETE CASCADE,
  FOREIGN KEY (shipment_id)
    REFERENCES shipmentlog
    ON DELETE CASCADE
);
-- shows the all products in shipment with the quantity, price and unit type
-- and supplier 
CREATE VIEW shipment AS(
  SELECT * FROM product_ship
  NATURAL JOIN shipmentLog
  NATURAL JOIN productLog
  NATURAL JOIN productb
  NATURAL JOIN supplierb
);

-- the components require the product, the supplier,
-- and the component name
-- Many-to-Many relationship between product and component through manufacturing
CREATE TABLE manufacturingb(
  manufacturing_id  number(5) GENERATED ALWAYS AS IDENTITY,
  product_id  number(5),
  supplier_id number(5),
  component   varchar(20) NOT NULL,
  PRIMARY KEY(manufacturing_id),
  FOREIGN KEY (product_id, supplier_id)
    REFERENCES productb(product_id, supplier_id)
    ON DELETE CASCADE
); 

CREATE VIEW manufacturing AS(
  SELECT * FROM manufacturingb
  NATURAL JOIN product
);
-- Drop existing views
DROP VIEW store;
DROP VIEW supplier;
DROP VIEW product;
DROP VIEW shipment;
DROP VIEW manufacturing;

-- Delete existing tables
DELETE FROM product_ship;
DELETE FROM manufacturingb;
DELETE FROM productb;
DELETE FROM productlog;
DELETE FROM shipmentLog;
DELETE FROM supplierb;
DELETE FROM storeb;
DELETE FROM building;
-- Drop existing tables
DROP TABLE product_ship;
DROP TABLE manufacturingb;
DROP TABLE productb;
DROP TABLE productlog;
DROP TABLE shipmentLog;
DROP TABLE supplierb;
DROP TABLE storeb;
DROP TABLE building;
SELECT * FROM product;
SELECT * FROM ALL_TABLES WHERE OWNER = 'DEL226';
SELECT * FROM ALL_VIEWS WHERE OWNER = 'DEL226';
SELECT * FROM building;
SELECT * FROM supplier;
SELECT * FROM supplier WHERE supplier_name LIKE '%Hello%' AND location like '%';
SELECT * FROM productlog;
SELECT * FROM product;
INSERT INTO productlog (product_name) VALUES ('ABC');
SELECT * FROM manufacturing;

ALTER TABLE product_ship DROP PRIMARY KEY;
-- Store 108
-- Supplier 5 (denny)
-- Product 3 (apple)
-- There are (Many/One) x to (One) y in Y
-- There are (Many/One) y to (One) x in X