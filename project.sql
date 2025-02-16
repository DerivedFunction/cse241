-- Each store is assigned a unique id.
-- Two stores can be in the same location,
-- but have different ids 
CREATE TABLE store(
  store_id 	number(5) GENERATED ALWAYS AS IDENTITY,
  location 	varchar(20) NOT NULL,
  PRIMARY KEY (store_id)
);

-- Each supplier is assigned a unique id.
-- The same supplier (name) can have multiple facilities
-- uniquely identified by its id
CREATE TABLE supplier(
  supplier_id 	  number(5) GENERATED ALWAYS AS IDENTITY,
  supplier_name		varchar(20),
  location	      varchar(20) NOT NULL,
  PRIMARY KEY (supplier_id)
);

-- Each generic product has its own id.
-- We can identify the origin by its supplier
-- Many-to-Many relationship between supplier and product
CREATE TABLE product(
  product_id	  number(5),
  supplier_id	  number(5),
  product_name	varchar(20) NOT NULL,
  price		      numeric(5,2) DEFAULT 0,
  PRIMARY KEY (product_id, supplier_id),
  FOREIGN KEY (supplier_id) 
    REFERENCES supplier(supplier_id)
    ON DELETE CASCADE
);

-- Each shipment is assigned a unique id
-- We will require the store, the supplier, and the product
-- Many-to-Many relationship between store and product through shipment
-- Many-to-Many relationship between supplier and product through shipment
CREATE TABLE shipment(
  shipment_id   number(5) GENERATED ALWAYS AS IDENTITY,
  store_id      number(5),
  supplier_id   number(5),
  product_id    number(5),   
  quantity      int DEFAULT 0 CHECK (quantity >= 0),
  unit_price    numeric(5,2) DEFAULT 0,
  ship_date     timestamp NOT NULL,
  PRIMARY KEY (shipment_id),
  FOREIGN KEY (product_id, supplier_id)
    REFERENCES product(product_id, supplier_id)
    ON DELETE CASCADE,
  FOREIGN KEY (store_id)
    REFERENCES store(store_id)
    ON DELETE CASCADE
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

-- CREATE ROLE manager;
-- GRANT ALL ON store, supplier, product, shipment, manufacturing TO manager;
-- CREATE OR REPLACE FUNCTION grant_select_to_supplier()
-- RETURNS void AS $$
-- DECLARE
--   supplier_rec RECORD;
-- BEGIN
--   FOR supplier_rec IN SELECT supplier_id, name FROM supplier LOOP
--     EXECUTE format('GRANT SELECT ON supplier TO %I', supplier_rec.name);
--     EXECUTE format('GRANT SELECT ON product TO %I WHERE supplier_id = %L', supplier_rec.name, supplier_rec.supplier_id);
--     EXECUTE format('GRANT SELECT ON shipment TO %I WHERE supplier_id = %L', supplier_rec.name, supplier_rec.supplier_id);
--     EXECUTE format('GRANT SELECT ON manufacturing TO %I WHERE supplier_id = %L', supplier_rec.name, supplier_rec.supplier_id);
--   END LOOP;
-- END;
-- $$ LANGUAGE plpgsql;

-- SELECT grant_select_to_supplier();
SELECT * FROM store;
INSERT INTO store(location) VALUES ('ABC')