-- ---------------------------------------------------------------------------
-- Book Worm — initial schema.
-- Runs on PostgreSQL 16 in prod and H2 (MODE=PostgreSQL) in tests.
-- Money is stored as integer paise (INR × 100). Timestamps are UTC.
-- ---------------------------------------------------------------------------

CREATE TABLE app_user (
  id               BIGSERIAL PRIMARY KEY,
  email            VARCHAR(255) NOT NULL UNIQUE,
  password_hash    VARCHAR(72)  NOT NULL,
  first_name       VARCHAR(80),
  last_name        VARCHAR(80),
  phone            VARCHAR(20),
  role             VARCHAR(16)  NOT NULL DEFAULT 'CUSTOMER',
  gift_points      INTEGER      NOT NULL DEFAULT 0,
  created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  CONSTRAINT ck_user_role CHECK (role IN ('GUEST','CUSTOMER','ADMIN')),
  CONSTRAINT ck_user_gift_points_nonneg CHECK (gift_points >= 0)
);
CREATE INDEX ix_user_email ON app_user(email);

CREATE TABLE address (
  id         BIGSERIAL PRIMARY KEY,
  user_id    BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  line1      VARCHAR(255) NOT NULL,
  line2      VARCHAR(255),
  city       VARCHAR(80)  NOT NULL,
  state      VARCHAR(80)  NOT NULL,
  pin        VARCHAR(10)  NOT NULL,
  country    VARCHAR(80)  NOT NULL DEFAULT 'India',
  phone      VARCHAR(20),
  is_default BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX ix_address_user ON address(user_id);

CREATE TABLE category (
  id        BIGSERIAL PRIMARY KEY,
  name      VARCHAR(80) NOT NULL,
  slug      VARCHAR(80) NOT NULL UNIQUE,
  parent_id BIGINT REFERENCES category(id) ON DELETE SET NULL
);

CREATE TABLE brand (
  id   BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  slug VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE author (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(160) NOT NULL,
  bio        TEXT,
  avatar_url VARCHAR(500)
);

CREATE TABLE book (
  id                      BIGSERIAL PRIMARY KEY,
  title                   VARCHAR(255) NOT NULL,
  author_id               BIGINT REFERENCES author(id) ON DELETE SET NULL,
  brand_id                BIGINT REFERENCES brand(id)  ON DELETE SET NULL,
  format                  VARCHAR(16) NOT NULL,
  language                VARCHAR(32) NOT NULL DEFAULT 'English',
  price_paise             INTEGER     NOT NULL,
  stock                   INTEGER     NOT NULL DEFAULT 0,
  cover_url               VARCHAR(500),
  description             TEXT,
  rating                  NUMERIC(2,1) NOT NULL DEFAULT 0.0,
  copies_sold             INTEGER     NOT NULL DEFAULT 0,
  tentative_delivery_days INTEGER     NOT NULL DEFAULT 5,
  created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  CONSTRAINT ck_book_format CHECK (format IN ('Paperback','HardCover','eBook')),
  CONSTRAINT ck_book_price_nonneg CHECK (price_paise >= 0),
  CONSTRAINT ck_book_stock_nonneg CHECK (stock >= 0)
);
CREATE INDEX ix_book_title   ON book(title);
CREATE INDEX ix_book_brand   ON book(brand_id);
CREATE INDEX ix_book_created ON book(created_at);
CREATE INDEX ix_book_copies  ON book(copies_sold);

CREATE TABLE book_category (
  book_id     BIGINT NOT NULL REFERENCES book(id)     ON DELETE CASCADE,
  category_id BIGINT NOT NULL REFERENCES category(id) ON DELETE CASCADE,
  PRIMARY KEY (book_id, category_id)
);

CREATE TABLE book_tag (
  book_id BIGINT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
  tag     VARCHAR(40) NOT NULL,
  PRIMARY KEY (book_id, tag)
);

CREATE TABLE review (
  id         BIGSERIAL PRIMARY KEY,
  book_id    BIGINT NOT NULL REFERENCES book(id)     ON DELETE CASCADE,
  user_id    BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  rating     INTEGER NOT NULL,
  text       TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT ux_review_per_user UNIQUE (book_id, user_id)
);
CREATE INDEX ix_review_book ON review(book_id);

CREATE TABLE cart (
  id      BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE cart_item (
  id       BIGSERIAL PRIMARY KEY,
  cart_id  BIGINT  NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
  book_id  BIGINT  NOT NULL REFERENCES book(id),
  quantity INTEGER NOT NULL,
  CONSTRAINT ck_cart_item_qty CHECK (quantity > 0),
  CONSTRAINT ux_cart_item_book UNIQUE (cart_id, book_id)
);

CREATE TABLE wishlist (
  id      BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE wishlist_item (
  id          BIGSERIAL PRIMARY KEY,
  wishlist_id BIGINT NOT NULL REFERENCES wishlist(id) ON DELETE CASCADE,
  book_id     BIGINT NOT NULL REFERENCES book(id),
  CONSTRAINT ux_wishlist_item_book UNIQUE (wishlist_id, book_id)
);

CREATE TABLE app_order (
  id                BIGSERIAL PRIMARY KEY,
  user_id           BIGINT      NOT NULL REFERENCES app_user(id),
  address_id        BIGINT      NOT NULL REFERENCES address(id),
  subtotal_paise    INTEGER     NOT NULL,
  tax_paise         INTEGER     NOT NULL,
  shipping_paise    INTEGER     NOT NULL,
  discount_paise    INTEGER     NOT NULL DEFAULT 0,
  gift_points_used  INTEGER     NOT NULL DEFAULT 0,
  total_paise       INTEGER     NOT NULL,
  status            VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  cancellable_until TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT ck_order_status CHECK (status IN ('PENDING','PAID','SHIPPED','DELIVERED','CANCELLED','RETURNED')),
  CONSTRAINT ck_order_totals_nonneg CHECK (
      subtotal_paise >= 0 AND tax_paise >= 0 AND shipping_paise >= 0
      AND discount_paise >= 0 AND gift_points_used >= 0 AND total_paise >= 0
  )
);
CREATE INDEX ix_order_user_created ON app_order(user_id, created_at);

CREATE TABLE order_item (
  id                BIGSERIAL PRIMARY KEY,
  order_id          BIGINT  NOT NULL REFERENCES app_order(id) ON DELETE CASCADE,
  book_id           BIGINT  NOT NULL REFERENCES book(id),
  quantity          INTEGER NOT NULL,
  price_at_purchase INTEGER NOT NULL,
  CONSTRAINT ck_order_item_qty CHECK (quantity > 0),
  CONSTRAINT ck_order_item_price CHECK (price_at_purchase >= 0)
);
CREATE INDEX ix_order_item_order ON order_item(order_id);

CREATE TABLE payment (
  id              BIGSERIAL PRIMARY KEY,
  order_id        BIGINT      NOT NULL REFERENCES app_order(id) ON DELETE CASCADE,
  method          VARCHAR(16) NOT NULL,
  status          VARCHAR(16) NOT NULL,
  transaction_ref VARCHAR(64) NOT NULL,
  amount_paise    INTEGER     NOT NULL,
  processed_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  CONSTRAINT ck_payment_method CHECK (method IN ('CREDIT','DEBIT','UPI','WALLET')),
  CONSTRAINT ck_payment_status CHECK (status IN ('SUCCESS','DECLINED','PENDING')),
  CONSTRAINT ck_payment_amount CHECK (amount_paise >= 0)
);
CREATE INDEX ix_payment_order ON payment(order_id);
