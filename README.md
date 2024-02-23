Войти в аккаунт
```fish
auth in <login> <password>
```

Зарегистрироваться
```fish
auth reg <login> <password>
```

Операции по изменению меню доступны только администратору

Добавить в меню блюдо
```fish
menu add <dish_name>
```

Убрать блюдо из меню
```fish
menu remove <dish_name>
```

Изменить цену блюда
```fish
menu edit price <dish_name> <price>
```

Изменить время готовки блюда
```fish
menu edit time <dish_name> <time>
```

Изменить количество доступных сейчас экземпляров блюда
```fish
menu edit count <dish_name> <count>
```

Вывести меню
```fish
menu list
```

Добавить блюдо в заказ
```fish
order add <dish_name>
```

Убрать блюдо из заказа, если оно ещё не готово
```fish
order remove <dish_name>
```

Заплатить за ещё не оплаченные блюда заказа
```fish
order pay
```

Вывести заказ
```fish
order list
```

Посмотреть общую выручку ресторана
```fish
order revenue
```

Выйти из программы
```fish
q
```