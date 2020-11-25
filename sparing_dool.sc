Program Sparing_Doll;
const
NameSkill  = 'Wrestling';
var
doll : Cardinal;

function FindDoll : Boolean;
begin
    FindDistance := 1;
    FindTypeEx($1074, $FFFF, Ground, False);
    if FindCount > 0 then
    begin
        AddToSystemJournal('Нашли куклу.');
        doll := FindItem;
        result := True;
        exit;
    end;
    AddToSystemJournal('ERROR: Не нашли куклу...');
    result := False;   
end;


begin
    if FindDoll then 
    begin
        While Dead or ( GetSkillValue(NameSkill) <= 80) do
        begin           
            CheckLag(60000);
            UseObject(doll);             
            wait(3000);
        end;    
    end;
end.
