-- ---------------------------------------------------------------------------
-- Book Worm full wireframe-accurate seed.
-- Replaces the branch-4 stub (V2 was deleted from disk on branch 9).
-- Runs on PostgreSQL 16 and on H2 in MODE=PostgreSQL.
--
-- Contains:
--   * 19 categories (sidebar order)
--   * 6 brands (publishers)
--   * 13 authors
--   * 29 books (9 from the wireframes + 20 filler for filter coverage)
--   * demo user demo@bookworm.io / Demo@123, 250 gift points, 1 saved address
--   * 3 sample orders (DELIVERED / SHIPPED / CANCELLED with cancel window closed)
-- ---------------------------------------------------------------------------

-- =====================  Categories (19)  =====================

INSERT INTO category (name, slug) VALUES
  ('Romance',                 'romance'),
  ('Mystery',                 'mystery'),
  ('Science Fiction',         'science-fiction'),
  ('Fantasy',                 'fantasy'),
  ('Historical',              'historical'),
  ('Biography',               'biography'),
  ('Self-help',               'self-help'),
  ('Memoir',                  'memoir'),
  ('Travel',                  'travel'),
  ('Cooking',                 'cooking'),
  ('Children''s',             'childrens'),
  ('Young Adult',             'young-adult'),
  ('Comics & Graphic Novels', 'comics-graphic-novels'),
  ('Poetry',                  'poetry'),
  ('Drama',                   'drama'),
  ('Science',                 'science'),
  ('Philosophy',              'philosophy'),
  ('Religion',                'religion'),
  ('Language Learning',       'language-learning');

-- =====================  Brands (publishers)  =====================

INSERT INTO brand (name, slug) VALUES
  ('Penguin Random House', 'penguin-random-house'),
  ('HarperCollins',        'harper-collins'),
  ('Bloomsbury',           'bloomsbury'),
  ('Simon & Schuster',     'simon-schuster'),
  ('Pan Macmillan',        'pan-macmillan'),
  ('Rupa Publications',    'rupa-publications');

-- =====================  Authors (13)  =====================

INSERT INTO author (name, bio) VALUES
  ('Arjun Patel',    'Bestselling self-help author focused on mindful productivity.'),
  ('Raj Patel',      'Sports psychologist and author of learning-optimisation classics.'),
  ('James Wright',   'Motivational speaker and executive coach turned author.'),
  ('James Adams',    'Award-winning mystery writer known for coastal-town thrillers.'),
  ('Jessica Martin', 'Romance novelist and former poet.'),
  ('Laura Mitchell', 'Hard-SF novelist with a physics PhD.'),
  ('Daniel Reed',    'Minimalist lifestyle blogger and Zen practitioner.'),
  ('Clara Nelson',   'Cozy-mystery favourite for a decade.'),
  ('Emilly Parker',  'Beloved children''s picture-book author and illustrator.'),
  ('Ruskin Bond',    'India''s most beloved writer of hill-station stories.'),
  ('Amitav Ghosh',   'Historical fiction author, Jnanpith Award winner.'),
  ('Chetan Bhagat',  'Bestselling contemporary Indian novelist.'),
  ('Sudha Murthy',   'Educator, philanthropist, and prolific author for all ages.');

-- =====================  Wireframe books (9)  =====================

INSERT INTO book (title, author_id, brand_id, format, language, price_paise, stock,
                  cover_url, description, rating, copies_sold, tentative_delivery_days)
VALUES
  ('The Art of Focus',
   (SELECT id FROM author WHERE name = 'Arjun Patel'),
   (SELECT id FROM brand  WHERE slug = 'penguin-random-house'),
   'Paperback', 'English', 34900, 40, '/covers/art-of-focus.jpg',
   'A practical guide to building deep-work habits in the age of distraction.',
   4.5, 120, 5),

  ('The Art of Learning',
   (SELECT id FROM author WHERE name = 'Raj Patel'),
   (SELECT id FROM brand  WHERE slug = 'harper-collins'),
   'Paperback', 'English', 39900, 35, '/covers/art-of-learning.jpg',
   'How to accelerate mastery: lessons from world-class chess and martial arts.',
   4.3, 95, 5),

  ('The Path to Success',
   (SELECT id FROM author WHERE name = 'James Wright'),
   (SELECT id FROM brand  WHERE slug = 'simon-schuster'),
   'HardCover', 'English', 59900, 25, '/covers/path-to-success.jpg',
   'A step-by-step career playbook from a Fortune 100 executive coach.',
   4.6, 140, 6),

  ('The Midnight Hour',
   (SELECT id FROM author WHERE name = 'James Adams'),
   (SELECT id FROM brand  WHERE slug = 'harper-collins'),
   'Paperback', 'English', 29900, 60, '/covers/midnight-hour.jpg',
   'A twist-a-page thriller set in a rain-drenched coastal town.',
   4.2, 90, 4),

  ('Beneath the Stars',
   (SELECT id FROM author WHERE name = 'Jessica Martin'),
   (SELECT id FROM brand  WHERE slug = 'bloomsbury'),
   'Paperback', 'English', 34900, 45, '/covers/beneath-the-stars.jpg',
   'A slow-burn small-town romance across two summers.',
   4.4, 85, 5),

  ('The Final Frontier',
   (SELECT id FROM author WHERE name = 'Laura Mitchell'),
   (SELECT id FROM brand  WHERE slug = 'pan-macmillan'),
   'HardCover', 'English', 64900, 20, '/covers/final-frontier.jpg',
   'A generation ship, an unreliable narrator, and a physics puzzle you have to solve.',
   4.7, 210, 6),

  ('Joy of Minimalism',
   (SELECT id FROM author WHERE name = 'Daniel Reed'),
   (SELECT id FROM brand  WHERE slug = 'penguin-random-house'),
   'Paperback', 'English', 27900, 55, '/covers/joy-of-minimalism.jpg',
   'Own less, live more. A gentle guide out of the buy-more spiral.',
   4.1, 75, 4),

  ('The Vanishing House',
   (SELECT id FROM author WHERE name = 'Clara Nelson'),
   (SELECT id FROM brand  WHERE slug = 'bloomsbury'),
   'Paperback', 'English', 32900, 30, '/covers/vanishing-house.jpg',
   'A quiet Sussex village. A house that shouldn''t be there. Detective Reed is puzzled.',
   4.5, 110, 5),

  ('The Lost Kitten',
   (SELECT id FROM author WHERE name = 'Emilly Parker'),
   (SELECT id FROM brand  WHERE slug = 'rupa-publications'),
   'HardCover', 'English', 24900, 70, '/covers/lost-kitten.jpg',
   'A warm bedtime tale about kindness and finding your way home.',
   4.8, 260, 3);

-- =====================  Filler books (20)  =====================

INSERT INTO book (title, author_id, brand_id, format, language, price_paise, stock,
                  cover_url, description, rating, copies_sold, tentative_delivery_days)
VALUES
  ('The Blue Umbrella',           (SELECT id FROM author WHERE name='Ruskin Bond'),   (SELECT id FROM brand WHERE slug='rupa-publications'),    'Paperback','English',19900,80,'/covers/blue-umbrella.jpg',       'A little girl and her prized blue umbrella in a hill town.',                       4.6, 180, 3),
  ('Rain in the Mountains',       (SELECT id FROM author WHERE name='Ruskin Bond'),   (SELECT id FROM brand WHERE slug='rupa-publications'),    'Paperback','English',24900,50,'/covers/rain-mountains.jpg',      'Notes from a life in the Himalayas.',                                              4.5, 130, 4),
  ('The Shadow Lines',            (SELECT id FROM author WHERE name='Amitav Ghosh'),  (SELECT id FROM brand WHERE slug='penguin-random-house'), 'Paperback','English',44900,25,'/covers/shadow-lines.jpg',        'Two generations, three cities, one indelible history.',                            4.4, 95, 5),
  ('Sea of Poppies',              (SELECT id FROM author WHERE name='Amitav Ghosh'),  (SELECT id FROM brand WHERE slug='penguin-random-house'), 'HardCover','English',69900,15,'/covers/sea-of-poppies.jpg',      'The first novel of the Ibis trilogy.',                                             4.6, 110, 6),
  ('Five Point Someone',          (SELECT id FROM author WHERE name='Chetan Bhagat'), (SELECT id FROM brand WHERE slug='rupa-publications'),    'Paperback','English',19900,90,'/covers/five-point.jpg',          'What not to do at IIT.',                                                           4.0, 320, 3),
  ('The 3 Mistakes of My Life',   (SELECT id FROM author WHERE name='Chetan Bhagat'), (SELECT id FROM brand WHERE slug='rupa-publications'),    'Paperback','English',19900,85,'/covers/3-mistakes.jpg',          'Cricket, business and love in Ahmedabad.',                                         3.9, 240, 3),
  ('Wise and Otherwise',          (SELECT id FROM author WHERE name='Sudha Murthy'),  (SELECT id FROM brand WHERE slug='penguin-random-house'), 'Paperback','English',22900,60,'/covers/wise-otherwise.jpg',      'Fifty short essays on people and life in India.',                                  4.4, 190, 4),
  ('How I Taught My Grandmother', (SELECT id FROM author WHERE name='Sudha Murthy'),  (SELECT id FROM brand WHERE slug='penguin-random-house'), 'Paperback','English',17900,75,'/covers/grandmother-read.jpg',    'True stories of love and learning.',                                               4.5, 160, 3),
  ('Mystery of the Silver Coin',  (SELECT id FROM author WHERE name='Clara Nelson'),  (SELECT id FROM brand WHERE slug='bloomsbury'),           'Paperback','English',28900,42,'/covers/silver-coin.jpg',         'A cosy village puzzle with a knitting-circle sleuth.',                             4.3, 65, 5),
  ('Beyond the Horizon',          (SELECT id FROM author WHERE name='Laura Mitchell'),(SELECT id FROM brand WHERE slug='pan-macmillan'),        'Paperback','English',34900,38,'/covers/beyond-horizon.jpg',      'A first-contact story that takes the physics seriously.',                          4.5, 88, 5),
  ('The Rose Garden',             (SELECT id FROM author WHERE name='Jessica Martin'),(SELECT id FROM brand WHERE slug='bloomsbury'),           'Paperback','English',29900,50,'/covers/rose-garden.jpg',         'Second-chance romance at a heritage estate.',                                      4.2, 70, 5),
  ('Silent Detective',            (SELECT id FROM author WHERE name='James Adams'),   (SELECT id FROM brand WHERE slug='harper-collins'),       'Paperback','English',27900,45,'/covers/silent-detective.jpg',    'A locked-room puzzle set in a snowbound country hotel.',                           4.1, 55, 5),
  ('Small Steps, Big Change',     (SELECT id FROM author WHERE name='Arjun Patel'),   (SELECT id FROM brand WHERE slug='penguin-random-house'), 'Paperback','English',31900,42,'/covers/small-steps.jpg',         'Habit design for people who fail at New Year resolutions.',                        4.4, 100, 4),
  ('Learning to Learn',           (SELECT id FROM author WHERE name='Raj Patel'),     (SELECT id FROM brand WHERE slug='harper-collins'),       'Paperback','English',34900,30,'/covers/learning-to-learn.jpg',   'Study smarter, not longer.',                                                       4.2, 80, 5),
  ('The Executive Playbook',      (SELECT id FROM author WHERE name='James Wright'),  (SELECT id FROM brand WHERE slug='simon-schuster'),       'HardCover','English',54900,20,'/covers/executive-playbook.jpg',  'Frameworks for first-time VPs.',                                                   4.3, 60, 6),
  ('Quiet Kitchen',               (SELECT id FROM author WHERE name='Daniel Reed'),   (SELECT id FROM brand WHERE slug='penguin-random-house'), 'HardCover','English',49900,25,'/covers/quiet-kitchen.jpg',       'Minimalist cooking. 40 recipes, 40 ingredients.',                                  4.4, 45, 5),
  ('Bunny Goes to School',        (SELECT id FROM author WHERE name='Emilly Parker'), (SELECT id FROM brand WHERE slug='rupa-publications'),    'HardCover','English',22900,80,'/covers/bunny-school.jpg',        'A first-day-of-school picture book.',                                              4.7, 200, 3),
  ('The Kitten Next Door',        (SELECT id FROM author WHERE name='Emilly Parker'), (SELECT id FROM brand WHERE slug='rupa-publications'),    'Paperback','English',18900,90,'/covers/kitten-next-door.jpg',    'A sequel to The Lost Kitten.',                                                     4.6, 180, 3),
  ('Kingdom of the Silver Wind',  (SELECT id FROM author WHERE name='Laura Mitchell'),(SELECT id FROM brand WHERE slug='pan-macmillan'),        'HardCover','English',59900,18,'/covers/silver-wind.jpg',         'Political fantasy with a coming-of-age heart.',                                    4.5, 70, 6),
  ('Poems for the Monsoon',       (SELECT id FROM author WHERE name='Ruskin Bond'),   (SELECT id FROM brand WHERE slug='rupa-publications'),    'Paperback','English',15900,55,'/covers/poems-monsoon.jpg',       'A slim volume of rainy-season poems.',                                             4.5, 50, 4);

-- =====================  Book ↔ Category  =====================

INSERT INTO book_category (book_id, category_id) VALUES
  ((SELECT id FROM book WHERE title='The Art of Focus'),           (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='The Art of Learning'),        (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='The Path to Success'),        (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='The Midnight Hour'),          (SELECT id FROM category WHERE slug='mystery')),
  ((SELECT id FROM book WHERE title='Beneath the Stars'),          (SELECT id FROM category WHERE slug='romance')),
  ((SELECT id FROM book WHERE title='The Final Frontier'),         (SELECT id FROM category WHERE slug='science-fiction')),
  ((SELECT id FROM book WHERE title='Joy of Minimalism'),          (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='The Vanishing House'),        (SELECT id FROM category WHERE slug='mystery')),
  ((SELECT id FROM book WHERE title='The Lost Kitten'),            (SELECT id FROM category WHERE slug='childrens')),
  ((SELECT id FROM book WHERE title='The Blue Umbrella'),          (SELECT id FROM category WHERE slug='childrens')),
  ((SELECT id FROM book WHERE title='Rain in the Mountains'),      (SELECT id FROM category WHERE slug='memoir')),
  ((SELECT id FROM book WHERE title='The Shadow Lines'),           (SELECT id FROM category WHERE slug='historical')),
  ((SELECT id FROM book WHERE title='Sea of Poppies'),             (SELECT id FROM category WHERE slug='historical')),
  ((SELECT id FROM book WHERE title='Five Point Someone'),         (SELECT id FROM category WHERE slug='young-adult')),
  ((SELECT id FROM book WHERE title='The 3 Mistakes of My Life'),  (SELECT id FROM category WHERE slug='young-adult')),
  ((SELECT id FROM book WHERE title='Wise and Otherwise'),         (SELECT id FROM category WHERE slug='biography')),
  ((SELECT id FROM book WHERE title='How I Taught My Grandmother'),(SELECT id FROM category WHERE slug='childrens')),
  ((SELECT id FROM book WHERE title='Mystery of the Silver Coin'), (SELECT id FROM category WHERE slug='mystery')),
  ((SELECT id FROM book WHERE title='Beyond the Horizon'),         (SELECT id FROM category WHERE slug='science-fiction')),
  ((SELECT id FROM book WHERE title='The Rose Garden'),            (SELECT id FROM category WHERE slug='romance')),
  ((SELECT id FROM book WHERE title='Silent Detective'),           (SELECT id FROM category WHERE slug='mystery')),
  ((SELECT id FROM book WHERE title='Small Steps, Big Change'),    (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='Learning to Learn'),          (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='The Executive Playbook'),     (SELECT id FROM category WHERE slug='self-help')),
  ((SELECT id FROM book WHERE title='Quiet Kitchen'),              (SELECT id FROM category WHERE slug='cooking')),
  ((SELECT id FROM book WHERE title='Bunny Goes to School'),       (SELECT id FROM category WHERE slug='childrens')),
  ((SELECT id FROM book WHERE title='The Kitten Next Door'),       (SELECT id FROM category WHERE slug='childrens')),
  ((SELECT id FROM book WHERE title='Kingdom of the Silver Wind'), (SELECT id FROM category WHERE slug='fantasy')),
  ((SELECT id FROM book WHERE title='Poems for the Monsoon'),      (SELECT id FROM category WHERE slug='poetry'));

-- =====================  Book tags  =====================

INSERT INTO book_tag (book_id, tag) VALUES
  ((SELECT id FROM book WHERE title='The Art of Focus'),       'productivity'),
  ((SELECT id FROM book WHERE title='The Art of Focus'),       'habits'),
  ((SELECT id FROM book WHERE title='The Midnight Hour'),      'thriller'),
  ((SELECT id FROM book WHERE title='The Midnight Hour'),      'suspense'),
  ((SELECT id FROM book WHERE title='The Final Frontier'),     'hard-sf'),
  ((SELECT id FROM book WHERE title='The Lost Kitten'),        'picture-book'),
  ((SELECT id FROM book WHERE title='Sea of Poppies'),         'award-winner'),
  ((SELECT id FROM book WHERE title='Joy of Minimalism'),      'lifestyle'),
  ((SELECT id FROM book WHERE title='Beneath the Stars'),      'small-town'),
  ((SELECT id FROM book WHERE title='The Path to Success'),    'career'),
  ((SELECT id FROM book WHERE title='Kingdom of the Silver Wind'), 'epic-fantasy');

-- =====================  Demo user (demo@bookworm.io / Demo@123)  =====================

INSERT INTO app_user (email, password_hash, first_name, last_name, phone, role, gift_points) VALUES
  ('demo@bookworm.io',
   '$2a$12$QBWMHEO6/TufG0lXh9fOZuQoQ4vBV.Mi8u8MgutA7zhKkF95zWSXK',
   'Priya', 'Sharma', '+91-98765-43210', 'CUSTOMER', 250);

INSERT INTO address (user_id, line1, line2, city, state, pin, country, phone, is_default) VALUES
  ((SELECT id FROM app_user WHERE email='demo@bookworm.io'),
   '12 MG Road, Indiranagar', 'Apt 4B', 'Bengaluru', 'Karnataka', '560038', 'India',
   '+91-98765-43210', TRUE);

INSERT INTO cart (user_id)     VALUES ((SELECT id FROM app_user WHERE email='demo@bookworm.io'));
INSERT INTO wishlist (user_id) VALUES ((SELECT id FROM app_user WHERE email='demo@bookworm.io'));

-- =====================  Three sample orders  =====================
-- 1) DELIVERED, 30 days old
-- 2) SHIPPED, 12 hours old (still within cancel window)
-- 3) CANCELLED, 5 days old (past cancel window)

-- Order 1: DELIVERED — Joy of Minimalism ×2, The Lost Kitten ×1
INSERT INTO app_order (user_id, address_id, subtotal_paise, tax_paise, shipping_paise,
                       discount_paise, gift_points_used, total_paise, status,
                       created_at, cancellable_until) VALUES
  ((SELECT id FROM app_user WHERE email='demo@bookworm.io'),
   (SELECT id FROM address  WHERE pin='560038'),
   80700,   -- 27900*2 + 24900
   9684,    -- floor(80700 * 0.12)
   0,       -- ≥ ₹499 so free
   0, 0,
   90384,   -- 80700 + 9684 + 0 - 0
   'DELIVERED',
   CURRENT_TIMESTAMP - INTERVAL '30' DAY,
   CURRENT_TIMESTAMP - INTERVAL '28' DAY);

INSERT INTO order_item (order_id, book_id, quantity, price_at_purchase) VALUES
  ((SELECT MAX(id) FROM app_order), (SELECT id FROM book WHERE title='Joy of Minimalism'), 2, 27900),
  ((SELECT MAX(id) FROM app_order), (SELECT id FROM book WHERE title='The Lost Kitten'),   1, 24900);

INSERT INTO payment (order_id, method, status, transaction_ref, amount_paise) VALUES
  ((SELECT MAX(id) FROM app_order), 'CREDIT', 'SUCCESS', 'TXN-SEED0001', 90384);


-- Order 2: SHIPPED — The Final Frontier ×1
INSERT INTO app_order (user_id, address_id, subtotal_paise, tax_paise, shipping_paise,
                       discount_paise, gift_points_used, total_paise, status,
                       created_at, cancellable_until) VALUES
  ((SELECT id FROM app_user WHERE email='demo@bookworm.io'),
   (SELECT id FROM address  WHERE pin='560038'),
   64900,
   7788,    -- floor(64900 * 0.12)
   0,
   0, 0,
   72688,
   'SHIPPED',
   CURRENT_TIMESTAMP - INTERVAL '12' HOUR,
   CURRENT_TIMESTAMP + INTERVAL '36' HOUR);

INSERT INTO order_item (order_id, book_id, quantity, price_at_purchase) VALUES
  ((SELECT MAX(id) FROM app_order), (SELECT id FROM book WHERE title='The Final Frontier'), 1, 64900);

INSERT INTO payment (order_id, method, status, transaction_ref, amount_paise) VALUES
  ((SELECT MAX(id) FROM app_order), 'UPI', 'SUCCESS', 'TXN-SEED0002', 72688);


-- Order 3: CANCELLED, 5 days old — exercises the "cancel window closed" path in the UI
INSERT INTO app_order (user_id, address_id, subtotal_paise, tax_paise, shipping_paise,
                       discount_paise, gift_points_used, total_paise, status,
                       created_at, cancellable_until) VALUES
  ((SELECT id FROM app_user WHERE email='demo@bookworm.io'),
   (SELECT id FROM address  WHERE pin='560038'),
   34900,
   4188,
   4900,
   0, 0,
   43988,
   'CANCELLED',
   CURRENT_TIMESTAMP - INTERVAL '5' DAY,
   CURRENT_TIMESTAMP - INTERVAL '3' DAY);

INSERT INTO order_item (order_id, book_id, quantity, price_at_purchase) VALUES
  ((SELECT MAX(id) FROM app_order), (SELECT id FROM book WHERE title='The Art of Focus'), 1, 34900);
