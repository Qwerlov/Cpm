package com.oleo.cpmscripts.nodes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NodeTypes {
   public static final Map<String, NodeType> ALL = new LinkedHashMap();
   public static final List<String> CATEGORIES = List.of("Входы", "Логика", "Время", "Константы", "Действия");

   private static void reg(NodeType t) {
      ALL.put(t.id, t);
   }

   public static NodeType byId(String id) {
      return (NodeType)ALL.get(id);
   }

   public static List<NodeType> byCategory(String cat) {
      List<NodeType> l = new ArrayList();

      for(NodeType t : ALL.values()) {
         if (t.category.equals(cat)) {
            l.add(t);
         }
      }

      return l;
   }

   static {
      reg(NodeType.create("item_held", "Предмет в руке", "Входы").out("out", "есть", NodeType.PType.BOOL).param(NodeType.item("item", "Предмет")).param(NodeType.dropdown("hand", "Рука", new String[]{"main", "off", "any"}, new String[]{"Основная", "Вторая", "Любая"}, "main")));
      reg(NodeType.create("item_use", "Использование предмета", "Входы").out("out", "использует", NodeType.PType.BOOL).param(NodeType.item("item", "Предмет")).param(NodeType.dropdown("mode", "Режим", new String[]{"while", "press"}, new String[]{"Пока использует", "По началу использования"}, "while")));
      reg(NodeType.create("chat_in", "Чат: сообщение", "Входы").out("new", "новое сообщение", NodeType.PType.BOOL).out("msg", "текст", NodeType.PType.STRING).out("num", "число в тексте", NodeType.PType.NUMBER));
      reg(NodeType.create("chat_match", "Проверка строки", "Логика").in("msg", "текст (пусто = чат)", NodeType.PType.STRING).in("find", "образец (пусто = параметр)", NodeType.PType.STRING).out("out", "совпало", NodeType.PType.BOOL).param(NodeType.text("text", "Фраза", "")).param(NodeType.dropdown("mode", "Тип совпадения", new String[]{"partial", "full"}, new String[]{"Частичное (содержит)", "Полное (равно)"}, "partial")));
      reg(NodeType.create("equip", "Надето (броня)", "Входы").out("out", "надето", NodeType.PType.BOOL).param(NodeType.dropdown("slot", "Слот", new String[]{"head", "chest", "legs", "feet"}, new String[]{"Голова", "Грудь", "Ноги", "Ступни"}, "feet")).param(NodeType.item("item", "Предмет")));
      reg(NodeType.create("effect", "Эффект зелья", "Входы").out("has", "есть эффект", NodeType.PType.BOOL).out("amp", "уровень (0+)", NodeType.PType.NUMBER).param(NodeType.effect("effect", "Эффект")));
      reg(NodeType.create("entities", "Сущности рядом", "Входы").out("count", "количество", NodeType.PType.NUMBER).out("has", "есть рядом", NodeType.PType.BOOL).param(NodeType.entity("type", "Тип (пусто = все)")).param(NodeType.text("name", "Имя содержит (пусто = все)", "")).param(NodeType.num("radius", "Радиус", (double)8.0F, (double)1.0F, (double)64.0F)));
      reg(NodeType.create("key", "Кейбинд", "Входы").out("out", "нажат", NodeType.PType.BOOL).param(NodeType.text("n", "Клавиша (g, space...)", "g")).param(NodeType.dropdown("mode", "Режим", new String[]{"down", "pressed"}, new String[]{"Зажата", "По нажатию"}, "down")));
      reg(NodeType.create("mouse", "Мышь", "Входы").out("attack", "ЛКМ зажата", NodeType.PType.BOOL).out("attackPress", "ЛКМ нажата", NodeType.PType.BOOL).out("attackRelease", "ЛКМ отпущена", NodeType.PType.BOOL).out("use", "ПКМ зажата", NodeType.PType.BOOL).out("usePress", "ПКМ нажата", NodeType.PType.BOOL).out("useRelease", "ПКМ отпущена", NodeType.PType.BOOL));
      reg(NodeType.create("hit", "Удар попал", "Входы").out("entity", "по существу", NodeType.PType.BOOL).out("block", "по блоку", NodeType.PType.BOOL));
      reg(NodeType.create("block_use", "Блок: взаимодействие", "Входы").out("new", "кликнул по блоку", NodeType.PType.BOOL).out("id", "id блока", NodeType.PType.STRING).out("match", "совпало", NodeType.PType.BOOL).param(NodeType.block("block", "Блок (пусто = любой)")));
      reg(NodeType.create("pose", "Поза / состояние", "Входы").out("out", "активно", NodeType.PType.BOOL).param(NodeType.dropdown("state", "Состояние", new String[]{"sneak", "walking", "sprint", "swim", "crawl", "sleep", "elytra", "ride", "falling", "onGround", "inWater", "onFire"}, new String[]{"Крадётся", "Идёт пешком", "Бежит", "Плавает", "Ползёт", "Лежит (спит)", "Элитры", "Едет на транспорте", "Падает", "На земле", "В воде", "Горит"}, "sneak")));
      reg(NodeType.create("vital", "Здоровье / голод / воздух", "Входы").out("out", "верно", NodeType.PType.BOOL).param(NodeType.dropdown("stat", "Показатель", new String[]{"health", "food", "air", "saturation", "absorption"}, new String[]{"Здоровье", "Голод", "Воздух", "Насыщение", "Поглощение"}, "health")).param(NodeType.dropdown("cmp", "Сравнение", new String[]{"<", "<=", "==", ">=", ">"}, new String[]{"<", "≤", "=", "≥", ">"}, "<=")).param(NodeType.num("value", "Порог", (double)10.0F, (double)0.0F, (double)1000.0F)));
      reg(NodeType.create("move", "Движение", "Входы").out("out", "движется", NodeType.PType.BOOL).param(NodeType.num("speed", "Порог скорости, блок/тик", 0.05, (double)0.0F, (double)2.0F)));
      reg(NodeType.create("and", "И (AND)", "Логика").in("a", "A", NodeType.PType.BOOL).in("b", "B", NodeType.PType.BOOL).out("out", "A и B", NodeType.PType.BOOL));
      reg(NodeType.create("or", "ИЛИ (OR)", "Логика").in("a", "A", NodeType.PType.BOOL).in("b", "B", NodeType.PType.BOOL).out("out", "A или B", NodeType.PType.BOOL));
      reg(NodeType.create("not", "НЕ (NOT)", "Логика").in("in", "вход", NodeType.PType.BOOL).out("out", "не вход", NodeType.PType.BOOL));
      reg(NodeType.create("xor", "Искл. ИЛИ (XOR)", "Логика").in("a", "A", NodeType.PType.BOOL).in("b", "B", NodeType.PType.BOOL).out("out", "A xor B", NodeType.PType.BOOL));
      reg(NodeType.create("nor", "НЕ-ИЛИ (NOR)", "Логика").in("a", "A", NodeType.PType.BOOL).in("b", "B", NodeType.PType.BOOL).out("out", "не (A или B)", NodeType.PType.BOOL));
      reg(NodeType.create("nand", "НЕ-И (NAND)", "Логика").in("a", "A", NodeType.PType.BOOL).in("b", "B", NodeType.PType.BOOL).out("out", "не (A и B)", NodeType.PType.BOOL));
      reg(NodeType.create("edge", "Фронт (стало истинно)", "Логика").in("in", "вход", NodeType.PType.BOOL).out("out", "импульс", NodeType.PType.BOOL));
      reg(NodeType.create("toggle", "Триггер (переключатель)", "Логика").in("in", "переключить", NodeType.PType.BOOL).out("out", "состояние", NodeType.PType.BOOL));
      reg(NodeType.create("latch", "Защёлка (Set/Reset)", "Логика").in("set", "Set", NodeType.PType.BOOL).in("reset", "Reset", NodeType.PType.BOOL).out("out", "состояние", NodeType.PType.BOOL));
      reg(NodeType.create("timer", "Таймер (удержание)", "Время").in("in", "вход", NodeType.PType.BOOL).out("out", "истина", NodeType.PType.BOOL).param(NodeType.num("sec", "Секунд удержания", (double)2.0F, 0.05, (double)60.0F)));
      reg(NodeType.create("delay", "Задержка", "Время").in("in", "вход", NodeType.PType.BOOL).out("out", "истина", NodeType.PType.BOOL).param(NodeType.num("sec", "Секунд задержки", (double)1.0F, 0.05, (double)60.0F)));
      reg(NodeType.create("cooldown", "Cooldown (не чаще N сек)", "Время").in("in", "вход", NodeType.PType.BOOL).out("out", "пропущено", NodeType.PType.BOOL).param(NodeType.num("sec", "Пауза, сек", (double)3.0F, 0.1, (double)600.0F)));
      reg(NodeType.create("cycle", "Цикл (каждые N тиков)", "Время").in("in", "вход (гейт)", NodeType.PType.BOOL).out("out", "импульс", NodeType.PType.BOOL).param(NodeType.num("ticks", "Период, тиков", (double)20.0F, (double)2.0F, (double)1200.0F)));
      reg(NodeType.create("const_num", "Число", "Константы").out("out", "значение", NodeType.PType.NUMBER).param(NodeType.num("value", "Значение", (double)128.0F, (double)0.0F, (double)255.0F)));
      reg(NodeType.create("compare", "Сравнение чисел", "Константы").in("a", "A", NodeType.PType.NUMBER).in("b", "B", NodeType.PType.NUMBER).out("out", "верно", NodeType.PType.BOOL).param(NodeType.dropdown("cmp", "Операция", new String[]{"<", "<=", "==", ">=", ">"}, new String[]{"<", "≤", "=", "≥", ">"}, ">=")));
      reg(NodeType.create("play", "Проиграть анимацию", "Действия").in("in", "вход", NodeType.PType.BOOL).param(NodeType.anim("anim", "Анимация")).param(NodeType.dropdown("mode", "Режим", new String[]{"once", "while"}, new String[]{"По срабатыванию", "Пока истинно (стоп при сбросе)"}, "once")));
      reg(NodeType.create("stop", "Остановить анимацию", "Действия").in("in", "вход", NodeType.PType.BOOL).param(NodeType.anim("anim", "Анимация")));
      reg(NodeType.create("set_layer", "Установить value-слой", "Действия").in("in", "вход", NodeType.PType.BOOL).in("value", "значение", NodeType.PType.NUMBER).param(NodeType.layer("layer", "Слой")).param(NodeType.dropdown("mode", "Режим", new String[]{"while", "once"}, new String[]{"Пока истинно", "По срабатыванию"}, "while")).param(NodeType.dropdown("release", "При сбросе входа", new String[]{"nothing", "reset"}, new String[]{"Ничего", "Сброс слоя в 128"}, "nothing")));
      reg(NodeType.create("chat", "Сообщение в чат", "Действия").in("in", "вход", NodeType.PType.BOOL).param(NodeType.text("text", "Текст", "привет!")).param(NodeType.dropdown("target", "Куда", new String[]{"self", "server"}, new String[]{"Только себе (в чат)", "Отправить на сервер"}, "self")));
   }
}
