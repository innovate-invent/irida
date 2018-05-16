import React from "react";
import { Button } from "antd";

const { i18n } = window.PAGE;

export class SaveTemplateButton extends React.Component {
  constructor(props) {
    super(props);
  }

  showSaveModal = e => {
    e.stopPropagation();
    this.props.showSaveModal();
  };

  render() {
    return this.props.modified !== null ? (
      <Button size="small" type="dashed" onClick={this.showSaveModal}>
        {i18n.linelist.templates.saveModified}
      </Button>
    ) : null;
  }
}
