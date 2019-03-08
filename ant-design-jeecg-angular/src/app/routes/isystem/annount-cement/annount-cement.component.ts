import { Component, OnInit, ViewChild } from '@angular/core';
import { _HttpClient, ModalHelper } from '@delon/theme';
import { STColumn, STComponent, STData } from '@delon/abc';
import { SFSchema } from '@delon/form';
import { IsystemAnnountAddComponent } from './annount-add/annount-add.component';
import { IsystemAnnountEditComponent } from './annount-edit/annount-edit.component';
import { NzMessageService } from 'ng-zorro-antd';

@Component({
  selector: 'app-isystem-annount-cement',
  templateUrl: './annount-cement.component.html',
})
export class IsystemAnnountCementComponent implements OnInit {
  url = `sys/annountCement/list?field=id,,,titile,startTime,endTime,sender,priority,msgType,sendStatus,sendTime,cancelTime,action`;
  searchSchema: SFSchema = {
    properties: {
      titile: {
        type: 'string',
        title: '标题'
      },
      msgContent: {
        type: 'string',
        title: '内容'
      }
    }
  };
  @ViewChild('st') st: STComponent;
  columns: STColumn[] = [
    { title: '', index: 'id', type: 'checkbox' },
    { title: '#', type: 'no' },
    { title: '标题', index: 'titile' },
    { title: '开始时间', type: 'date', index: 'startTime' },
    { title: '开始时间', type: 'date', index: 'endTime' },
    { title: '发布人', index: 'sender' },
    {
      title: '优先级',
      index: 'priority',
      format:this.getDict
    },
    { title: '公告类型', index: 'msgType',format:this.getDict },
    { title: '发布状态', index: 'sendStatus',format:this.getDict },
    { title: '发布时间', type: 'date', index: 'sendTime' },
    { title: '撤销时间', type: 'date', index: 'cancelTime' },
    {
      title: '',
      buttons: [
        // { text: '查看', click: (item: any) => `/form/${item.id}` },
        {
          text: '编辑', icon: 'edit', type: 'modal',
          modal: {
            component: IsystemAnnountEditComponent,
          },
          click: (record: any, modal: any) => {
            this.st.reload();
          }
        },
      ]
    }
  ];

  constructor(private http: _HttpClient, private message: NzMessageService, private modal: ModalHelper) { }

  ngOnInit() { }

  add() {
    this.modal
      .createStatic(IsystemAnnountAddComponent, { i: {} })
      .subscribe(() => this.st.reload());
  }
  getDict(item: STData, col: STColumn):string{
    const dict={
      priority:{
        L:'低',
        M:'中',
        H:'高'
      },
      msgType:{
        ALL:'全体用户',
        M:'指定用户'
      },
      sendStatus:{
        1:'已发布',
        0:'未发布'
      }
    }
    
    return dict[col.indexKey][item[col.indexKey]]
  }

}
