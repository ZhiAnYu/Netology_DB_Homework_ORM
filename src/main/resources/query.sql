-- 1. Поиск name и surname пользователей из MOSCOW
SELECT name, surname FROM PERSONS WHERE city_of_living = 'MOSCOW';

-- 2. Поиск всех полей, где age > 27, отсортировано по убыванию возраста
SELECT * FROM PERSONS WHERE age > 27 ORDER BY age DESC;