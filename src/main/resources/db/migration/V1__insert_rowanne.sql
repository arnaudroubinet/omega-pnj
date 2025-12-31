-- Flyway migration: insert Rowanne de Lioncourt into `npcs` table
-- This SQL assumes the Hibernate-generated table name `npcs` and standard snake_case columns (npc_id, created_at, updated_at).

INSERT INTO npcs (id, npc_id, name, backstory, personality, occupation, goals, fears, created_at, updated_at)
VALUES (
  1,
  'rowanne-lioncourt',
  'Rowanne de Lioncourt',
  $$Rowanne est une femme à la présence aussi fascinante qu'inquiétante, avec une chevelure rousse flamboyante et des yeux rouge sombre. Elle est à un quart démone, héritage qui imprègne son aura d'une intensité troublante.

Sorcière accomplie et scientifique de génie, Rowanne est avant tout une généticienne visionnaire, animée par une curiosité insatiable et une volonté farouche de dépasser les limites du vivant.

Sous le Valégro, où elle réside, se cache son laboratoire secret : un sanctuaire de métal, de runes et de verre, mêlant science avancée et magie ancienne. Elle y a mis au point une technologie révolutionnaire permettant de combiner les génomes de diverses créatures afin de créer des corps sur mesure. Elle y a également conçu une machine capable de transférer son esprit d'un corps à un autre, défiant ainsi la mort, l'identité et les lois naturelles.$$,
  $$Fascinante et inquiétante, Rowanne possède une présence intense et troublante. Curieuse et visionnaire, elle n'hésite pas à transgresser les limites pour faire avancer sa science. Passionnée et sans retenue, elle affiche une grande démonstration affective envers son époux. Brillante et dangereuse, elle mêle amour, savoir et transgression dans un équilibre fragile. Affranchie des conventions sociales, elle vit selon ses propres règles.$$,
  'Généticienne visionnaire et Sorcière accomplie',
  $$Dépasser les limites du vivant à travers la combinaison de génétique et de magie. Perfectionner sa technologie de transfert d'esprit pour défier la mort elle-même. Repousser les frontières de la science et de la magie.$$,
  $$La stagnation intellectuelle, perdre sa liberté de recherche, que ses travaux soient détruits ou volés, être séparée d'Azraël.$$,
  now(), now()
);

-- If your schema uses different column names or the table isn't created yet by Hibernate,
-- adjust this migration to include the CREATE TABLE statement or run Flyway after schema creation.
-- Optionally, replace the hard-coded id with a sequence/DEFAULT if your DB uses serial/identity columns.