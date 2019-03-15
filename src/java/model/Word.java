package model;

import java.io.Serializable;

/**
 * @author Joti
 */
public class Word implements Serializable {

  private String topic;
  private String text;
  private int level;

  public Word() {
  }

  public Word(String topic, String text, int level) {
    this.topic = topic;
    this.text = text;
    this.level = level;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

}
