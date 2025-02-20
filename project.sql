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
    product_id	  number(5),
    product_name  varchar(20) NOT NULL,
    PRIMARY KEY (product_id)
);

-- Each supplier can have the same product, with its own 
-- unit type and price
-- We can identify the origin by its supplier
CREATE TABLE product(
  product_id	  number(5),
  supplier_id	  number(5),
  price		      numeric(5,2) DEFAULT 0,
  unit_type     varchar(2) DEFAULT 'ea', -- default to each
  PRIMARY KEY (product_id, supplier_id),
  FOREIGN KEY (supplier_id) 
    REFERENCES supplierb
    ON DELETE CASCADE,
  FOREIGN KEY (product_id) 
    REFERENCES productlog
    ON DELETE CASCADE
);

-- Each shipment have one supplier and to one location at certain time
-- Each of those shipment is assigned a unique id
CREATE TABLE shipmentLog(
  shipment_id   number(5) GENERATED ALWAYS AS IDENTITY,
  supplier_id   number(5),
  to_id         number(5),
  ship_date     timestamp NOT NULL,
  PRIMARY KEY (shipment_id),
  FOREIGN KEY (to_id) 
    REFERENCES building(id)
    ON DELETE CASCADE,
  FOREIGN KEY (supplier_id) 
    REFERENCES supplierb
    ON DELETE CASCADE
);
-- Multiple products can be for a specfic shipment.
CREATE TABLE product_ship(
  shipment_id number(5),
  product_id number(5),
  qty           numeric(5,2) DEFAULT 0,
  PRIMARY KEY (shipment_id, product_id, qty),
  FOREIGN KEY (product_id)
    REFERENCES productLog
    ON DELETE CASCADE,
  FOREIGN KEY (shipment_id)
    REFERENCES shipmentLog
    ON DELETE CASCADE
);
-- shows the all products in shipment with the quantity, price and unit type
-- and supplier 
CREATE VIEW shipment AS(
  SELECT * FROM product_ship p
  NATURAL JOIN shipmentLog h
);

-- the components require the product, the supplier,
-- and the component name
-- Many-to-Many relationship between product and component through manufacturing
CREATE TABLE manufacturing(
  product_id  number(5),
  supplier_id number(5),
  component   varchar(20),
  PRIMARY KEY(product_id, supplier_id, component),
  FOREIGN KEY (product_id, supplier_id)
    REFERENCES product(product_id, supplier_id)
    ON DELETE CASCADE
); 

DELETE FROM product;
DELETE FROM store;
DELETE FROM supplier;
DELETE FROM manufacturing;
DELETE FROM shipment;
DELETE FROM building;
DROP TABLE product_ship;
DROP TABLE store;
DROP TABLE supplier;
DROP TABLE manufacturing;
DROP TABLE shipment;
DROP TABLE building;