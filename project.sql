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

CREATE VIEW productView AS
SELECT *
FROM product p
INNER JOIN supplier s ON p.supplier_id = s.supplier_id

CREATE VIEW shipmentView AS
SELECT *
FROM shipment t
INNER JOIN product p ON t.product_id = p.product_id AND t.supplier_id = p.supplier_id
INNER JOIN supplier s ON t.supplier_id = s.supplier_id
INNER JOIN store r ON t.store_id = r.store_id;

CREATE VIEW material AS
SELECT *
FROM manufacturing m
INNER JOIN product p ON m.product_id = p.product_id AND m.supplier_id = p.supplier_id
INNER JOIN supplier s ON m.supplier_id = s.supplier_id;