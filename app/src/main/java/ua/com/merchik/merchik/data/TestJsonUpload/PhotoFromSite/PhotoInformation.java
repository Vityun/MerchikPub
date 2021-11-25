package ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite;

import java.util.List;

public class PhotoInformation {



    /*
mod=images_view
act=set_dvi

data=array
внутри data
element_id - твой код элемента, Под которым будет возвращен ответ
id - код фото
state - состояние ДВИ (0 - отключено, 1 - включено)

——————————————————————

mod=images_view
act=set_comment

data=array
внутри data
element_id - твой код элемента, Под которым будет возвращен ответ
id - код фото
text - содержание комментария

——————————————————————

mod=images_view
act=set_premium

data=array
внутри data
element_id - твой код элемента, Под которым будет возвращен ответ
id - код фото
cash - сумма премии
reason - основание для выдачи премии

——————————————————————
сохранение оценок к фото

mod= images_view
act= set_score
в параметрах массив data по аналогии с планом работ update_data
в массиве data каэждый элемент отдельное фото

в нём передаёшь
id - ID фото
score - оценка
score - комментарий к оценке
*/

    public String mod;
    public String act;
    public List<PhotoInformationData> data;


}
