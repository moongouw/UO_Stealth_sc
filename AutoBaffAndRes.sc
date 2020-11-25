// Create by nepret   
////////﻿///////////////////////////////////////////////////////////
//
//                       [Stealth] AutoBaffAndRes v1.4
//
///////////////////////// MIDDLE-EARTH.RU /////////////////////////
//
// Скрипт Автоматического Бафа, ребафа и резуректа игроков. (Для дома)
//
// Функционал:
// Каст на игрока Bless + Protection
// Перекастовка Bless
// Воскрешение игрока Resurrection 
// (настройка любого количества фраз на полный баф и ребаф блесса)
//
// *Настройка +Есть -Нет
// Оповещение о том что заканчивается реагенты\еда. Вывод информации о том что кончается и сколько реагентов осталось.(+)
// Проверка маны перед каждым бафом. На каждый каст, своя проверка на мин-е кол-во маны для этого каста. (-)
// Проверка на наличие реагентов для каста. Если искомых не найдено, выводит сообщения каких именно реагентов нет. (-)
//  *** Настройка сумки с реагентами. (+)
//  *** Ожидание появления реагентов в сумке\на полу(берёт с пола, перекладывает в сумку).(-)
// Дистанция при которой бафер будет реагировать на команды: 4 тайла. (+)
//
// Для работы скрипта необходимо:
//
// Настроеть под себя переменные ниже.
// Делать то, что просит бафер(скрипт).
//
// Спасибо за помощь в отладке скрипта: MeSSiR'y и Kallish'у.
///////////////////////////////////////////////////////////////////
Program AutoBaffAndRes;
const
///////////////////////////////////////////////////////////////////
////////////////////// Настройка переменных ///////////////////////
                       //
WorkDistance = 4;      // Дистанция при которой реагировать на команды.
                       // на команды.
/////////////////////////
                       //
                       // Фразы при котором давать полный обкаст.
FullCastPh = 'баф';    // Указать одну или несколько фраз, 
                       // несколько фраз указывать через разделитель "|".
                       //
/////////////////////////
                       //
                       // Фразы при котором давать дополнительный блесс.
ReCastBless = 'блес';  // Указать одну или несколько фраз, 
                       // несколько фраз указывать через разделитель "|".
                       //
/////////////////////////
                       //
                       // При каком минимальном колличестве реагентов,
RegsCountInfo = 30;    // предупреждать в чат о том что он заканчивается?
                       // 0-Не предупреждать.
                       //
/////////////////////////
                       //
                       // Если реагенты хранятся в контейнере(на чаре),
                       // то укажите его ID. Если в бекпаке, то 0.
                       // Указывать только так:
ResCount = $7E895CA7;  // Бекпак -> Сумка с реагентами
                       // Вариант :
                       // Бекпак -> Сумка -> Сумка с реагентами
                       // не прокатит...
                       //
///////////////////////////////////////////////////////////////////
                       // Если не знаете как этим пользоваться,
                       // оставте 0.
DebugMode = 0;         // Выдавать информацию в системный журнал.
                       // Включать при отслеживании ошибок в скрипте.
                       //
///////////////////////// Конец настроек //////////////////////////
///////////////////////////////////////////////////////////////////

var
fbjtime, rbjtime : TDateTime;
t, ResCountVar, ID : integer;
regs : array [0..5] of cardinal;
regsname : array [0..5] of String;
{$Include 'all.inc'}


procedure CheckResInfo;
var
i, r, rc, h : integer;
Info, CountInfo, CountInfoFood : string;
begin
  if not Connected then exit;
  if RegsCountInfo < 1 then exit;
  if not Connected then exit;
  for i := 0 to 5 do
    begin
      if not Connected then exit;
      if i = 5 then h := Backpack;
      if i < 5 then h := ResCountVar;
      FindTypeEx(regs[i], $FFFF, h, false);
      if FindFullQuantity > RegsCountInfo then rc := rc + 1;
    end;
  if rc >= 6 then exit;
  rc := 0;
  if not Connected then exit;
  for i := 0 to 5 do
    begin
      if i = 5 then h := Backpack;
      if i < 5 then h := ResCountVar;
      FindTypeEx(regs[i], $FFFF, h, false);
      if (FindFullQuantity <= RegsCountInfo) and (FindFullQuantity > 0) then
        begin
          if not Connected then exit;
          if  i < 5 then
            begin
              CountInfo := CountInfo+' | '++IntToStr(FindFullQuantity);
              info := Info+' | '++ regsname[i];
              r := 1;
            end
          else
            begin
              CountInfoFood := 'Еда: '+IntToStr(FindFullQuantity);
            end;
          rc := 1;
        end;
    end;
  checksave;
  if rc = 1 then
    begin
      UOSay('Заканчивается: '+CountInfoFood);
      if r = 1 then
        begin
          UOSay(info+' | ');
          UOSay(CountInfo+' | ');
        end;
      if not Connected then exit;
    end;
end;

procedure CheckRes;
var
t, ts, c, i, y, rc, orc : integer;
info, ci : string;
begin
  if not Connected then exit;
  for i := 0 to 4 do
    begin
      if not Connected then exit;
      FindTypeEx(regs[i], $FFFF, ResCountVar, false);
      if FindFullQuantity > 0 then rc := rc + 1;
    end;
  if rc >= 5 then exit;
  y := 0;
  repeat
    rc := 0;
    for i := 0 to 4 do
      begin
        if not Connected then exit;
        FindTypeEx(regs[i], $FFFF, ResCountVar, false);
        if FindFullQuantity > 0 then rc := rc + 1;
        if FindFullQuantity < 1 then
          begin
            FindTypeEx(regs[i], $FFFF, Ground, false);
            if FindCount > 0 then
              begin
                if not Connected then exit;
                MoveItem(finditem, 0, ResCountVar, 0,0,0);
                td := 0;
                t := 0;
                repeat
                  if not Connected then exit;
                  checksave;
                  if Debug(10,100) then AddToSystemJournal('>>> Debug <<< Блок CheckRes > 1 цикл (105)');
                  wait(100);
                  t := t + 1;
                  FindTypeEx(regs[i], $FFFF, ResCountVar, false);
                until (FindFullQuantity > 0) or (t >= 20);
                UOSay('Взял "'+ regsname[i] +'"!');
                if not t <= 50 then rc := rc + 1;
                if orc = 1 then orc := 0;
              end
            else
              begin
                if y = 0 then c := c + 1;
                info := info+' '++ regsname[i];
              end;
          end;
      end;
    if rc >= 5 then UOSay('Работаем..');
    if rc >= 5 then exit;
    if ts = 0 then
      begin
        if c = 1 then ci := 'кончился';
        if c > 1 then ci := 'кончились';
        UOSay('У меня '+ ci +':');
        UOSay(info);
        UOSay('Кинь мне под ноги, или закинь в ручную.');
        wait(3000);
      end;
    if ts >= 166 then ts := 83;
    if (ts = 83) or (orc = 0) then 
      begin
        if orc = 0 then orc := 1;
        checksave;
        hungry(backpack);
        UOSay(info);
      end;
    info := '';
    checksave;
    wait(100);
    ts := ts + 1;
    if not Connected then exit; 
  until false;
end;

function CheckMP(MinMP : Integer) : Boolean;
begin
  if not Connected then exit;
  if Mana < MinMP then
    begin
      if not Connected then exit;
      result := true;
      UOSay('Маны малавато... надо бы по медитировать ()');
      td := 0;
      repeat
        if not Connected then exit;
        checksave;
        if Debug(60,2000) then AddToSystemJournal('>>> Debug <<< Блок CheckMP > 1 цикл (160)');
        UseSkill('Meditation');
        wait(2000);
        UOSay('Mana '+ IntToStr(Mana) +'/'+ IntToStr(MaxMana));
      until Mana = MaxMana;
      UOSay('Ну вот, другое дело :)'); 
    end;
  if not Connected then exit;
end;

function CheckTarget(SecTime : integer) : Boolean;
begin
  SecTime := SecTime * 10;
  td := 0;
  t := 0;
  repeat
    if not Connected then exit;
    checksave;
    if Debug(SecTime/10,100) then AddToSystemJournal('>>> Debug <<< Блок CheckTarget > 1 цикл (178)');
    wait(100);
    t := t + 1;
  until TargetPresent or (t >= SecTime);
  if t >= SecTime then result := false;
  if not Connected then exit;
  if TargetPresent then result := true;
end;

procedure Resurrect;
var
i : integer;
PlayerGhost : array [0..1] of cardinal;
begin
  PlayerGhost[0] := $0192;
  PlayerGhost[1] := $0193;
  for i := 0 to 1 do
    begin
      if not Connected then exit;
      CheckRes;
      FindTypeEx(PlayerGhost[i], $FFFF, Ground, False);
      if FindCount > 0 then
        begin
          repeat
            if not Connected then exit;
            if CheckMP(50) then
              begin
                if not Connected then exit;
                FindTypeEx(PlayerGhost[i], $FFFF, Ground, False);
                if FindCount < 1 then exit;
              end;
            if not Connected then exit;
            CancelTarget;
            Cast('Resurrection');
            if not CheckTarget(6) then
              begin
                if DebugMode = 1 then AddToSystemJournal('>>> Debug <<< Блок Resurrect > 1 цикл ожидания таргета (214)');
                exit;
              end;
            TargetToObject(finditem);
            UOSay('Mana '+ IntToStr(Mana) +'/'+ IntToStr(MaxMana));
            td := 0;
            t := 0;
            repeat
              if not Connected then exit;
              checksave;
              if Debug(5,100) then AddToSystemJournal('>>> Debug <<< Блок Resurrect > 2 цикл (224)');
              wait(100);
              t := t + 1;
            until (GetHP(finditem) > 0) or (t >= 50);
            if GetHP(finditem) = 0 then UOSay('Ты кнопку жать будеш или как?! Если нет, то отойди!');
            FindTypeEx(PlayerGhost[i], $FFFF, Ground, False);
            if not Connected then exit;
          until (GetHP(finditem) > 0) or (FindCount < 1);
        end;
    end;
  if not Connected then exit;
end;

procedure Recast;
begin
  if InJournalBetweenTimes(ReCastBless, rbjtime, Now) <> -1 then
    begin
      if not Connected then exit;
      ID := LineID;
      CheckRes;
      if ID <= 0 then exit;
      if LineName = 'System' then exit;
      if GetZ(self) <> GetZ(ID) then exit;
      if (GetX(ID) < GetX(self) - WorkDistance) or (GetX(ID) > GetX(self) + WorkDistance) or (GetY(ID) < GetY(self) - WorkDistance) or (GetY(ID) > GetY(self) + WorkDistance) then
        begin
          td := 0;
          t := 0;
          repeat
            if not Connected then exit;
            checksave;
            if Debug(5,100) then AddToSystemJournal('>>> Debug <<< Блок Recast > 1 цикл (254)');
            wait(100);
            t := t + 1;
          until (GetX(ID) >= GetX(self) - WorkDistance) and (GetX(ID) <= GetX(self) + WorkDistance) and (GetY(ID) >= GetY(self) - WorkDistance) and (GetY(ID) <= GetY(self) + WorkDistance) or (t >= 20);
          if t >= 20 then exit;
          if not Connected then exit;
        end;
      if CheckMP(9) then
        begin
        if (GetZ(self) <> GetZ(ID)) or (GetX(ID) < GetX(self) - WorkDistance) or (GetX(ID) > GetX(self) + WorkDistance) or (GetY(ID) < GetY(self) - WorkDistance) or (GetY(ID) > GetY(self) + WorkDistance) then
          begin
            UOSay(' ');
            UOSay('ммм...');
            UOSay('Я тебя потерял...');
            rbjtime := Now;
            exit;
          end;
        end;
      if not Connected then exit;
      CancelTarget;
      Cast('Bless');
      if not CheckTarget(6) then
        begin
          if DebugMode = 1 then AddToSystemJournal('>>> Debug <<< Блок Recast > 1 цикл ожидания таргета (277)');
          exit;
        end;
      UOSay('Mana '+ IntToStr(Mana) +'/'+ IntToStr(MaxMana));
      TargetToObject(ID);
      CheckRes;
      rbjtime := Now;
      CheckResInfo;
    end;
  if not Connected then exit;
end;

procedure FullBaff;
begin
  if not Connected then exit;
  if InJournalBetweenTimes(FullCastPh, fbjtime, Now) <> -1 then
    begin
      if not Connected then exit;
      ID := LineID;
      CheckRes;
      if ID <= 0 then exit;
      if LineName = 'System' then exit;
      if GetZ(self) <> GetZ(ID) then exit;
      if (GetX(ID) < GetX(self) - WorkDistance) or (GetX(ID) > GetX(self) + WorkDistance) or (GetY(ID) < GetY(self) - WorkDistance) or (GetY(ID) > GetY(self) + WorkDistance) then
        begin
          td := 0;
          t := 0;
          repeat
            if not Connected then exit;
            checksave;
            if Debug(5,100) then AddToSystemJournal('>>> Debug <<< Блок FullBaff > 1 цикл (307)');
            wait(100);
            t := t + 1;
          until (GetX(ID) >= GetX(self) - WorkDistance) and (GetX(ID) <= GetX(self) + WorkDistance) and (GetY(ID) >= GetY(self) - WorkDistance) and (GetY(ID) <= GetY(self) + WorkDistance) or (t >= 20);
          if t >= 20 then exit;
        end;
      if not Connected then exit;
      if CheckMP(15) then
        begin
          if (GetZ(self) <> GetZ(ID)) or (GetX(ID) < GetX(self) - WorkDistance) or (GetX(ID) > GetX(self) + WorkDistance) or (GetY(ID) < GetY(self) - WorkDistance) or (GetY(ID) > GetY(self) + WorkDistance) then
            begin
              UOSay(' ');
              UOSay('ммм...');
              UOSay('Я тебя потерял...');
              fbjtime := Now;
              exit;
          end;
        end;
      if not Connected then exit;
      CancelTarget;
      Cast('Protection');
      if not CheckTarget(6) then
        begin
          if DebugMode = 1 then AddToSystemJournal('>>> Debug <<< Блок FullBaff > 1 цикл ожидания таргета (330)');
          exit;
        end;
      TargetToObject(ID);
      if not Connected then exit;
      CancelTarget;
      Cast('Bless');
      if not CheckTarget(6) then
        begin
          if DebugMode = 1 then AddToSystemJournal('>>> Debug <<< Блок FullBaff > 2 цикл ожидания таргета (339)');
          exit;
        end;
      UOSay('Mana '+ IntToStr(Mana) +'/'+ IntToStr(MaxMana));
      TargetToObject(ID);
      CheckRes;
      fbjtime := Now;
      CheckResInfo;
    end;
  if not Connected then exit;  
end;

procedure CheckSettingGag;
begin
  if not Connected then WaitConnection(5);
  if ResCount > 0 then
    begin
      ResCountVar := ResCount;
      UseObject(Backpack);
      td := 0;
      t := 0;
      repeat
        if not Connected then exit;
        checksave;
        if Debug(6,100) then AddToSystemJournal('>>> Debug <<< Блок CheckSettingGag > 1 цикл (363)');
        wait(100);
        t := t + 1;
      until (LastContainer = Backpack) or (t >= 50);
      UseObject(ResCountVar);
      td := 0;
      t := 0;
      repeat
        if not Connected then exit;
        checksave;
        if Debug(6,100) then AddToSystemJournal('>>> Debug <<< Блок CheckSettingGag > 2 цикл (373)');
        wait(100);
        t := t + 1;
      until (LastContainer = ResCountVar) or (t >= 50);
    end
  else
    begin
      ResCountVar := Backpack;
      UseObject(ResCountVar);
      td := 0;
      t := 0;
      repeat
        if not Connected then exit;
        checksave;
        if Debug(6,100) then AddToSystemJournal('>>> Debug <<< Блок CheckSettingGag > 3 цикл (387)');
        wait(100);
        t := t + 1;
      until (LastContainer = ResCountVar) or (t >= 50);
    end;
end;

begin
  regs[0] := $0F85;
  regs[1] := $0F84;
  regs[2] := $0F8C;
  regs[3] := $0F86;
  regs[4] := $0F7B;
  regs[5] := Food;
  regsname[0] := 'GI';
  regsname[1] := 'GA';
  regsname[2] := 'SA';
  regsname[3] := 'MR';
  regsname[4] := 'BM';
  regsname[5] := 'Провиант';
  if not GetARStatus then SetARStatus(true);
  fbjtime := Now;
  rbjtime := Now;
  CheckSettingGag;
  repeat
    if not Connected then
      begin
        fbjtime := Now;
        rbjtime := Now;
        WaitConnection(5);
        CheckSettingGag;
      end;
    hungry(backpack);
    CheckRes;
    FullBaff;
    Recast;
    Resurrect;
    wait(100);
  until false;
end.