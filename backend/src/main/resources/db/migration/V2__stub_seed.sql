-- ---------------------------------------------------------------------------
-- Stub seed so /categories, /brands, /books return non-empty responses
-- during branch 4 development. The full wireframe-accurate seed replaces
-- this on feature/seed-and-tests (branch 9).
-- ---------------------------------------------------------------------------

INSERT INTO category (name, slug) VALUES
  ('Self-help',       'self-help'),
  ('Mystery',         'mystery'),
  ('Romance',         'romance'),
  ('Science Fiction', 'science-fiction'),
  ('Children''s',     'childrens');

INSERT INTO brand (name, slug) VALUES
  ('Penguin Random House', 'penguin-random-house'),
  ('HarperCollins',        'harper-collins'),
  ('Bloomsbury',           'bloomsbury');

INSERT INTO author (name, bio) VALUES
  ('Arjun Patel',   'Bestselling self-help author focused on mindful productivity.'),
  ('James Adams',   'Award-winning mystery writer.'),
  ('Emilly Parker', 'Beloved children''s picture-book author.');

-- Books
INSERT INTO book (title, author_id, brand_id, format, language, price_paise, stock, cover_url, description, rating, copies_sold, tentative_delivery_days)
VALUES
  ('The Art of Focus',   1, 1, 'Paperback', 'English', 34900, 40,
    '/covers/art-of-focus.jpg',
    'A practical guide to building deep-work habits in the age of distraction.',
    4.5, 120, 5),
  ('The Midnight Hour',  2, 2, 'Paperback', 'English', 29900, 60,
    '/covers/midnight-hour.jpg',
    'A twist-a-page thriller set in a rain-drenched coastal town.',
    4.2, 90, 4),
  ('The Lost Kitten',    3, 3, 'HardCover', 'English', 24900, 70,
    '/covers/lost-kitten.jpg',
    'A warm bedtime tale about kindness and finding your way home.',
    4.8, 210, 3);

INSERT INTO book_category (book_id, category_id) VALUES
  (1, 1),  -- Art of Focus -> Self-help
  (2, 2),  -- Midnight Hour -> Mystery
  (3, 5);  -- Lost Kitten  -> Children's

INSERT INTO book_tag (book_id, tag) VALUES
  (1, 'productivity'),
  (1, 'mindfulness'),
  (2, 'thriller'),
  (2, 'suspense'),
  (3, 'picture-book'),
  (3, 'bedtime');
